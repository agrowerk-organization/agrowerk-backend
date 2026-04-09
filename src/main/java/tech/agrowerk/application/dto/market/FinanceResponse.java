package tech.agrowerk.application.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FinanceResponse(
        @JsonProperty("name")   String name,
        @JsonProperty("unit")   String unit,
        @JsonProperty("data")   List<Entry> data
) {
    public record Entry(
            @JsonProperty("date")  String date,
            @JsonProperty("value") String value
    ) {}
}