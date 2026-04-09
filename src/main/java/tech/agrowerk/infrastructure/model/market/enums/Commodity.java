package tech.agrowerk.infrastructure.model.market.enums;

import lombok.Getter;

@Getter
public enum Commodity {
    SOJA("Soja", "SOYBEANS"),
    MILHO("Milho", "CORN"),
    CAFE("Café", "COFFEE"),
    TRIGO("Trigo", "WHEAT"),
    ALGODAO("Algodão", "COTTON"),
    ACUCAR("Açúcar", "SUGAR"),
    BOI_GORDO("Boi gordo", null);

    private final String description;
    private final String alphaVantageFunction;

    Commodity(String description, String alphaVantageFunction) {
        this.description = description;
        this.alphaVantageFunction = alphaVantageFunction;
    }

    public boolean hasAlphaVantageSource() {
        return alphaVantageFunction != null;
    }
}
