package tech.agrowerk.infrastructure.model.inventory.enums;

public enum StockType {
    INPUT("Insumo"),
    PRODUCTION("Produção");

    private final String description;

    StockType(String description) {
        this.description = description;
    }

    public String getDescription(String description) {
        return description;
    }
}
