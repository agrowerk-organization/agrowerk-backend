package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.agrowerk.application.dto.market.PtaxEntry;
import tech.agrowerk.application.dto.market.PtaxResponse;
import tech.agrowerk.infrastructure.exception.local.MarketDataException;
import tech.agrowerk.infrastructure.model.market.ExchangeRate;
import tech.agrowerk.infrastructure.repository.market.ExchangeRateRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExchangeRateService {

    private final RestTemplate restTemplate;
    private final ExchangeRateRepository exchangeRateRepository;

    private static final String PTAX_PERIOD_URL =
            "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/" +
                    "CotacaoMoedaPeriodo(moeda='USD',dataInicial=@start,dataFinalCotacao=@end)" +
                    "?@start='{start}'&@end='{end}'&$top=5000&$format=json&$select=cotacaoVenda,dataHoraCotacao";

    public ExchangeRateService(RestTemplate restTemplate,
                               ExchangeRateRepository exchangeRateRepository) {
        this.restTemplate = restTemplate;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public void backfillHistoricalRates(LocalDate from, LocalDate to) {
        log.info("Starting PTAX historical backfill from {} to {}", from, to);

        try {
            String startStr = from.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
            String endStr = to.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));

            log.debug("Fetching PTAX period data from BCB API...");

            PtaxResponse response = restTemplate.getForObject(
                    PTAX_PERIOD_URL,
                    PtaxResponse.class,
                    Map.of("start", startStr, "end", endStr)
            );

            if (response != null && response.value() != null) {
                int savedCount = 0;
                int skippedCount = 0;

                for (PtaxEntry entry : response.value()) {
                    LocalDate refDate = LocalDate.parse(entry.dataHoraCotacao().substring(0, 10));

                    if (!exchangeRateRepository.existsByCurrencyPairAndReferenceDate("USD_BRL", refDate)) {
                        exchangeRateRepository.save(ExchangeRate.builder()
                                .currencyPair("USD_BRL")
                                .referenceDate(refDate)
                                .rate(entry.cotacaoVenda())
                                .build());
                        savedCount++;
                    } else {
                        skippedCount++;
                    }
                }
                log.info("Backfill completed: {} new rates saved, {} already existed.", savedCount, skippedCount);
            } else {
                log.warn("BCB API returned an empty response for the requested period.");
            }

        } catch (Exception e) {
            log.error("Critical error during PTAX backfill: {}", e.getMessage());
            throw new MarketDataException("Failed to synchronize historical exchange rates", e);
        }
    }

    public BigDecimal getUsdToBrlForDate(LocalDate targetDate) {
        return exchangeRateRepository.findByCurrencyPairAndReferenceDate("USD_BRL", targetDate)
                .map(ExchangeRate::getRate)
                .orElseGet(() -> {
                            BigDecimal rate = fetchFromBcb(targetDate);
                            exchangeRateRepository.save(ExchangeRate.builder()
                                    .currencyPair("USD_BRL")
                                    .referenceDate(targetDate)
                                    .rate(rate)
                                    .build());
                            return rate;
                        });
    }

    @Cacheable(value = "excahngeRate", key = "'usd_brl_today")
    public BigDecimal getUsdToBrl() {
        log.info("Searching for the PTAX rate of the day (Cache MISS)");
        return fetchFromBcb(LocalDate.now());
    }

    public Map<LocalDate, BigDecimal> getRatesForPeriod(LocalDate start, LocalDate end) {
        log.info("Fetching exchange rates from database from period: {} to {}", start, end);

        List<ExchangeRate> rates = exchangeRateRepository
                .findAllByCurrencyPairAndReferenceDateBetween("USD_BRL", start, end);

        Map<LocalDate, BigDecimal> ratesMap = rates.stream()
                .collect(Collectors.toMap(
                        ExchangeRate::getReferenceDate,
                        ExchangeRate::getRate,
                        (existing, replacement) -> existing
                ));

        log.debug("Found {} rates in databse for the requested period", ratesMap.size());

        return ratesMap;
    }

    private BigDecimal fetchFromBcb(LocalDate targetDate) {
        for (int i = 0; i < 5; i++) {
            LocalDate date = targetDate.minusDays(i);
            try {
                String formatted = date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
                PtaxResponse response = restTemplate.getForObject(
                        PTAX_PERIOD_URL, PtaxResponse.class, Map.of("start", formatted, "end", formatted)
                );
                if (response != null && !response.value().isEmpty()) {
                    return response.value().getFirst().cotacaoVenda();
                }
            } catch (Exception e) {
                log.warn("PTAX unavailable for {}: {}", date, e.getMessage());
            }
        }
        throw new MarketDataException("Could not fetch USD/BRL rate for date: " + targetDate);
    }
}

