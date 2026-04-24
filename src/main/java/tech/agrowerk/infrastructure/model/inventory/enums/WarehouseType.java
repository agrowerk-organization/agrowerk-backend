package tech.agrowerk.infrastructure.model.inventory.enums;

public enum WarehouseType {
    SILO("Silo"),
    WAREHOUSE("Armazém"),
    COLD_STORAGE("Armazenamento refrigerado"),
    OPEN_YARD("Pátio aberto");

    private final String description;

    WarehouseType(String description) {
        this.description = description;
    }

    public String getDescription(String description) {
        return description;
    }
}
