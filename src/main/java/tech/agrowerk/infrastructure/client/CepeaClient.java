package tech.agrowerk.infrastructure.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tech.agrowerk.application.dto.market.CepeaPrice;
import tech.agrowerk.infrastructure.exception.local.MarketDataException;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@Slf4j
public class CepeaClient {

    private static final String SOJA_URL     = "/br/indicador/soja.aspx";
    private static final String MILHO_URL    = "/br/indicador/milho.aspx";
    private static final String FEIJAO_URL   = "/br/indicador/feijao.aspx";
    private static final String BOI_GORDO_URL= "/br/indicador/boi-gordo.aspx";
    private static final String CAFE_URL     = "/br/indicador/cafe.aspx";
    private static final String ALGODAO_URL  = "/br/indicador/algodao.aspx";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public CepeaClient(
            @Value("${cepea.api.base-url:https://www.cepea.esalq.usp.br}") String baseUrl,
            RestClient.Builder builder,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "Mozilla/5.0 (AgroWerk/1.0)")
                .defaultHeader("Accept", "application/vnd.ms-excel, */*")
                .build();

        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("cepeaCircuitBreaker");
        this.retry          = retryRegistry.retry("cepeaRetry");
        this.timeLimiter    = timeLimiterRegistry.timeLimiter("cepeaTimeLimiter");

        setupEventListeners();
    }

    public List<CepeaPrice> fetchSoja()     { return fetch(SOJA_URL,      "SOJA");      }
    public List<CepeaPrice> fetchMilho()    { return fetch(MILHO_URL,     "MILHO");     }
    public List<CepeaPrice> fetchFeijao()   { return fetch(FEIJAO_URL,   "FEIJAO");     }
    public List<CepeaPrice> fetchBoiGordo() { return fetch(BOI_GORDO_URL, "BOI_GORDO"); }
    public List<CepeaPrice> fetchCafe()     { return fetch(CAFE_URL,      "CAFE");      }
    public List<CepeaPrice> fetchAlgodao()  { return fetch(ALGODAO_URL,   "ALGODAO");   }


    private List<CepeaPrice> fetch(String url, String commodity) {
        log.debug("Fetching CEPEA data for commodity={}", commodity);

        try {
            return Decorators
                    .ofCompletionStage(() -> CompletableFuture.supplyAsync(
                            () -> downloadAndParse(url, commodity)))
                    .withRetry(retry, scheduler)
                    .withCircuitBreaker(circuitBreaker)
                    .withTimeLimiter(timeLimiter, scheduler)
                    .get()
                    .toCompletableFuture()
                    .join();

        } catch (Exception e) {
            log.error("Failed to fetch CEPEA data for {}: {}", commodity, e.getMessage());
            throw new MarketDataException("CEPEA API unavailable for " + commodity, e);
        }
    }

    private List<CepeaPrice> downloadAndParse(String url, String commodity) {
        byte[] xlsBytes = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("downloadXls", "1")
                        .build())
                .retrieve()
                .body(byte[].class);

        if (xlsBytes == null || xlsBytes.length == 0) {
            throw new MarketDataException("Empty response from CEPEA for " + commodity);
        }

        return parseXls(xlsBytes, commodity);
    }

    private List<CepeaPrice> parseXls(byte[] xlsBytes, String commodity) {
        List<CepeaPrice> records = new ArrayList<>();

        try (Workbook workbook = new HSSFWorkbook(new ByteArrayInputStream(xlsBytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String dateStr = getCellValue(row.getCell(0));
                    String priceStr = getCellValue(row.getCell(1));

                    if (dateStr == null || priceStr == null || dateStr.isBlank()) continue;

                    LocalDate date  = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
                    BigDecimal price = new BigDecimal(priceStr.trim().replace(",", "."));
                    String unit     = getCellValue(row.getCell(2));
                    String region   = getCellValue(row.getCell(3));

                    records.add(new CepeaPrice(commodity, price, unit, region, date));

                } catch (Exception e) {
                    log.warn("Skipping malformed row {} for {}: {}", i, commodity, e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new MarketDataException("Failed to parse CEPEA XLS for " + commodity, e);
        }

        log.info("Parsed {} records for {}", records.size(), commodity);
        return records;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER)
                    : String.valueOf(cell.getNumericCellValue());
            case BLANK   -> null;
            default      -> cell.toString();
        };
    }


    private void setupEventListeners() {
        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("CEPEA Circuit Breaker: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onFailureRateExceeded(event ->
                        log.error("CEPEA Circuit Breaker failure rate: {}%",
                                event.getFailureRate()));

        retry.getEventPublisher()
                .onRetry(event ->
                        log.warn("CEPEA retry attempt {}/{}",
                                event.getNumberOfRetryAttempts(),
                                retry.getRetryConfig().getMaxAttempts()))
                .onError(event ->
                {
                    assert event.getLastThrowable() != null;
                    log.error("❌ CEPEA all retries exhausted: {}",
                            event.getLastThrowable().getMessage());
                });
    }

    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }
}