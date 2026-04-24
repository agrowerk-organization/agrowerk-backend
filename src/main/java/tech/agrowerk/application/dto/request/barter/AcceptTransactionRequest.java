package tech.agrowerk.application.dto.request.barter;

import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;

public record AcceptTransactionRequest(
        Commodity commodity,
        BigDecimal basisUsd
) {
}
