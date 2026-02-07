package tech.agrowerk.application.dto.weather;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RealTimeUpdate(
        String eventType,
        UUID locationId,
        Current currentWeather,
        Alert alert,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {}
