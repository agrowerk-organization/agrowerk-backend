package tech.agrowerk.infrastructure.model.barter.enums;

public enum OfferType {
    CROP("Safra / Grãos"),
    ASSET("Ativo / Maquinário");

    private final String description;

    OfferType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}