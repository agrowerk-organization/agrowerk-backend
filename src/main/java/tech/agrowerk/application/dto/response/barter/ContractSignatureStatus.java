package tech.agrowerk.application.dto.response.barter;

import java.time.Instant;

public record ContractSignatureStatus(
        boolean offerorSigned,
        Instant offerorSignedAt,
        boolean acceptorSigned,
        Instant acceptorSignedAt
) {
}
