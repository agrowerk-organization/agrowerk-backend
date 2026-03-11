package tech.agrowerk.infrastructure.model.farming.enums;

public enum BrazilRegion {
    NORTHEAST("Nordeste"),
    NORTH("Norte"),
    SOUTHEAST("Sudeste"),
    MIDWEST("Centro-Oeste"),
    SOUTH("Sul"),
    SEMIARID("Semiárido"),
    MATOPIBA("MATOPIBA"),
    NATIONAL("Nacional");

    private final String description;

    BrazilRegion(String description) {
        this.description = description;
    }
}
