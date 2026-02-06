package tech.agrowerk.infrastructure.model.weather.enums;

import lombok.Getter;

@Getter
public enum WeatherAlertType {
    FROST("Geada", "Risco de formação de gelo nas plantas"),
    HEAT_WAVE("Onda de Calor", "Temperaturas extremamente altas"),
    HEAVY_RAIN("Chuva Intensa", "Alto volume de precipitação em curto período"),
    DROUGHT("Seca", "Baixa precipitação por período prolongado"),
    STRONG_WINDS("Ventos Fortes", "Velocidade do vento acima do normal"),
    HAIL("Granizo", "Risco de queda de granizo"),
    STORM("Tempestade", "Tempestade elétrica ou severa"),
    EXCESSIVE_MOISTURE("Umidade Excessiva", "Umidade do solo muito alta"),
    WATER_STRESS("Stress Hídrico", "Falta de água para as culturas"),
    PEST_FAVORABLE("Condições Favoráveis a Pragas", "Clima propício para proliferação de pragas"),
    DISEASE_FAVORABLE("Condições Favoráveis a Doenças", "Clima propício para doenças fúngicas"),
    OPTIMAL_PLANTING("Condições Ideais para Plantio", "Clima favorável para início de plantio"),
    OPTIMAL_HARVESTING("Condições Ideais para Colheita", "Clima favorável para colheita");

    private final String displayName;
    private final String description;

    WeatherAlertType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}