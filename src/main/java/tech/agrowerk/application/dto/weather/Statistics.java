package tech.agrowerk.application.dto.weather;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record Statistics(
        BigDecimal avgTemperatureLast7Days,
        BigDecimal totalRainfallLast7Days,
        BigDecimal totalRainfallLast30Days,
        BigDecimal avgHumidityLast7Days,
        Integer totalAlerts,
        Integer criticalAlerts,
        BigDecimal waterStressIndex,
        String waterStressLevel,
        BigDecimal evapotranspirationTotal7d
) {}