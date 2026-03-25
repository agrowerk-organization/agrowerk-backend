package tech.agrowerk.application.dto.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FinanceResponse(
        @JsonProperty("chart") Chart chart
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Chart(
            @JsonProperty("result") List<Result> result
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            @JsonProperty("meta") Meta meta
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            @JsonProperty("currency") String currency,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("regularMarketPrice") BigDecimal regularMarketPrice,
            @JsonProperty("previousClose") BigDecimal previousClose,
            @JsonProperty("fiftyTwoWeekHigh") BigDecimal fifttTwoWeekHigh,
            @JsonProperty("fiftyTwoWeekLow") BigDecimal fiftyTwoWeekLow,
            @JsonProperty("regularMarketTime") Long regularMarketTime,
            @JsonProperty("shortName") String shortName
    ) {}

    public Meta getMeta() {
        if (chart == null || chart.result() == null || chart.result().isEmpty()) return null;
        return chart.result().getFirst().meta();
    }
}
