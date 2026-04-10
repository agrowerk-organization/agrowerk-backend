package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.agrowerk.application.dto.market.PtaxResponse;
import tech.agrowerk.infrastructure.exception.local.MarketDataException;
import tech.agrowerk.infrastructure.model.market.ExchangeRate;
import tech.agrowerk.infrastructure.repository.market.ExchangeRateRepository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Slf4j
public class ExchangeRateService {

    private final RestTemplate restTemplate;
    private final ExchangeRateRepository exchangeRateRepository;

    private static final String PTAX_URL =
            "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/" +
                    "CotacaoDolarDia(dataCotacao=@dataCotacao)" +
                    "?@dataCotacao='{date}'&$top=1&$format=json&$select=cotacaoVenda";

    public ExchangeRateService(RestTemplate restTemplate,
                               ExchangeRateRepository exchangeRateRepository) {
        this.restTemplate = restTemplate;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public void backfillHistoricalRates(LocalDate from, LocalDate to) {
        LocalDate date = from;
        int saved = 0;

        while (!date.isAfter(to)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                if (!exchangeRateRepository.existsByCurrencyPairAndReferenceDate("USD_BRL", date)) {
                    try {
                        BigDecimal rate = fetchFromBcb(date);
                        exchangeRateRepository.save(ExchangeRate.builder()
                                .currencyPair("USD_BRL")
                                .referenceDate(date)
                                .rate(rate)
                                .build());
                        saved++;
                    } catch (Exception e) {
                        log.warn("No PTAX rate for {}: {}", date, e.getMessage());
                    }
                }
            }
            date = date.plusDays(1);
        }
        log.info("Exchange rate backfill complete: {} rates saved", saved);
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

    public BigDecimal getUsdToBrl() {
        return fetchFromBcb(LocalDate.now());
    }

    private BigDecimal fetchFromBcb(LocalDate targetDate) {
        for (int i = 0; i < 5; i++) {
            LocalDate date = targetDate.minusDays(i);
            try {
                String formatted = date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
                PtaxResponse response = restTemplate.getForObject(
                        PTAX_URL, PtaxResponse.class, Map.of("date", formatted)
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

