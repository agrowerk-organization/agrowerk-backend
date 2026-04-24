package tech.agrowerk.infrastructure.model.inventory.enums;

public enum AssetValuationMethod {
    FIXED_VALUE("Valor fixo"),
    COMMODITY_LINKED("Ligação de mercadoria"),
    MARKET_APPRAISAL("Avaliação de mercado");

    private final String description;

    AssetValuationMethod(String description) {
        this.description = description;
    }

    public String getDescription(String description) {
        return description;
    }
}
