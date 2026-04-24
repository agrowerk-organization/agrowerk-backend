package tech.agrowerk.infrastructure.model.inventory.enums;

public enum AssetCategory {
    EQUIPMENT("Equipamento"),
    TOOL("Ferramenta"),
    INPUT("Insumo"),
    SEED("Semente"),
    FERTILIZER("Fertilizante"),
    PESTICIDE("Pesticida"),
    OTHER("Outros");

    private final String description;

    AssetCategory(String description) {
        this.description = description;
    }

    public String getDescription(String description) {
        return description;
    }
}
