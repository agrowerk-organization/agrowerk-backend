package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.agrowerk.application.dto.market.PtaxResponse;
import tech.agrowerk.infrastructure.exception.local.MarketDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Slf4j
public class ExchangeRateService {

    private final RestTemplate restTemplate;

    private static final String PTAX_URL =
            "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/" +
            "CotacaoDolarDia(dataCotacao=@dataCotacao)" +
            "?@dataCotacao='{date}'&$top=1&$format=json&$select=cotacaoVenda";

    public ExchangeRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getUsdToBrl() {

        for (int i = 0; i < 5; i++) {
            LocalDate date = LocalDate.now().minusDays(i);

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

        throw new MarketDataException("Could not fetch USD/BRL rate from BCB");
    }
}
