package tech.agrowerk.infrastructure.security.enums;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@Getter
@AllArgsConstructor
public enum RateLimitProfile {

    SENSITIVE(
            Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1))),
            Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1)))
    ),

    AUTHENTICATED(
            Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))),
            Bandwidth.classic(3000, Refill.intervally(3000, Duration.ofHours(1)))
    ),

    PUBLIC(
            Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1))),
            Bandwidth.classic(200, Refill.intervally(200, Duration.ofHours(1)))
    ),

    GENERAL(
            Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1))),
            Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofHours(1)))
    );

    private final Bandwidth primaryLimit;
    private final Bandwidth burstLimit;
}
