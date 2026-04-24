package tech.agrowerk.infrastructure.model.inventory.enums;

public enum HazardLevel {
    LOW("Baixo"),
    MODERATE("Moderado"),
    HIGH("Alto"),
    CRITICAL("Crítico");

    private final String description;

    HazardLevel(String description) {
        this.description = description;
    }

    public String getDescription(String description) {
        return description;
    }
}
