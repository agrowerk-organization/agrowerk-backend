package tech.agrowerk.infrastructure.model.inventory.enums;

public enum AssetCondition {
    NEW("Novo"),
    EXCELLENT("Excelente"),
    GOOD("Bom"),
    FAIR("Justo"),
    POOR("Pobre");

    private final String description;

    AssetCondition(String description) {
        this.description = description;
    }

    public String getDescription(String description) {
        return description;
    }
}
