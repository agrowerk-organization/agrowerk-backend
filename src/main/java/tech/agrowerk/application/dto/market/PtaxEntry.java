package tech.agrowerk.application.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PtaxEntry(
        @JsonProperty("cotacaoVenda")
        BigDecimal cotacaoVenda
) {
}
