package tech.agrowerk.application.dto.weather.location;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record WeatherLocationDto(
        UUID id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String state,
        String country,
        String timezone,
        UUID propertyId,
        String propertyName,
        Boolean active,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant updatedAt
) {}