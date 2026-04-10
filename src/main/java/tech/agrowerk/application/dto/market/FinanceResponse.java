package tech.agrowerk.application.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FinanceResponse(
        @JsonProperty("observations") List<Entry> observations
) {
    public record Entry(
            @JsonProperty("date")  String date,
            @JsonProperty("value") String value
    ) {}
}