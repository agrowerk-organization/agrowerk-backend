package tech.agrowerk.infrastructure.model.market.enums;

public enum Commodity {
    SOJA("Soja"),
    MILHO("Milho"),
    BOI_GORDO("Boi gordo"),
    CAFE("Café"),
    TRIGO("Trigo"),
    ALGODAO("Algodão");

    private String description;

    Commodity(String description) {
        this.description = description;
    }
}
