package tech.agrowerk.infrastructure.model.farming.enums;

public enum BrazilRegion {
    NORTHEAST("Nordeste"),
    NORTH("Norte"),
    SOUTHEAST("Sudeste"),
    MIDWEST("Centro-Oeste"),
    SOUTH("Sul");

    private final String description;

    BrazilRegion(String description) {
        this.description = description;
    }
}
