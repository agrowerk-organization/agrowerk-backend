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
            Bandwidth.builder().capacity(3).refillIntervally(3, Duration.ofMinutes(1)).build(),
            Bandwidth.builder().capacity(10).refillIntervally(10, Duration.ofHours(1)).build()
    ),

    AUTHENTICATED(
            Bandwidth.builder().capacity(100).refillIntervally(100, Duration.ofMinutes(1)).build(),
            Bandwidth.builder().capacity(3000).refillIntervally(3000, Duration.ofHours(1)).build()
    ),

    PUBLIC(
            Bandwidth.builder().capacity(20).refillIntervally(20, Duration.ofMinutes(1)).build(),
            Bandwidth.builder().capacity(200).refillIntervally(200, Duration.ofHours(1)).build()
    ),

    GENERAL(
            Bandwidth.builder().capacity(60).refillIntervally(60, Duration.ofMinutes(1)).build(),
            Bandwidth.builder().capacity(1000).refillIntervally(1000, Duration.ofHours(1)).build()
    );

    private final Bandwidth primaryLimit;
    private final Bandwidth burstLimit;
}