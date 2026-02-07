package tech.agrowerk.application.dto.weather;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record Dashboard(
        UUID locationId,
        String locationName,
        BigDecimal latitude,
        BigDecimal longitude,
        Current current,
        List<Forecast> hourlyForecast,
        List<Forecast> dailyForecast,
        List<Alert> activeAlerts,
        Statistics statistics,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime lastUpdate
) {}