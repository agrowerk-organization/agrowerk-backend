package tech.agrowerk.application.dto.weather;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record Alert(
        UUID id,
        UUID locationId,
        String locationName,
        String alertType,
        String alertTypeDisplayName,
        String severity,
        String severityDisplayName,
        String severityColor,
        String title,
        String description,
        String recommendedActions,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant startTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant endTime,
        Boolean isActive,
        Boolean notified,
        String source,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant createdAt
) {}
