package tech.agrowerk.infrastructure.model.weather.enums;

import lombok.Getter;

@Getter
public enum WeatherAlertSeverity {
    LOW("Baixo", 1, "#4CAF50"),
    MEDIUM("Médio", 2, "#FF9800"),
    HIGH("Alto", 3, "#FF5722"),
    CRITICAL("Crítico", 4, "#D32F2F");

    private final String displayName;
    private final int level;
    private final String colorHex;

    WeatherAlertSeverity(String displayName, int level, String colorHex) {
        this.displayName = displayName;
        this.level = level;
        this.colorHex = colorHex;
    }
}
