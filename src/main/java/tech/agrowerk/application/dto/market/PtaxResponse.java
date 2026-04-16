package tech.agrowerk.application.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PtaxResponse(
        @JsonProperty("value")
        List<PtaxEntry> value
) {
}
