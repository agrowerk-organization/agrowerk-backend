package tech.agrowerk.infrastructure.model.shared_enums;

public enum UnitOfMeasure {
    KILOGRAM("kg", "Kilogram"),
    LITER("L", "Liter"),
    BAG("sc", "Bag"),
    UNIT("un", "Unit"),
    TON("t", "Ton"),
    MILLILITER("ml", "Milliliter"),
    GRAM("g", "Gram"),
    HECTARE("ha", "Hectare"),
    METERS("m", "Meters");

    private final String abbreviation;
    private final String description;

    UnitOfMeasure(String abbreviation, String description) {
        this.abbreviation = abbreviation;
        this.description = description;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getDescription() {
        return description;
    }
}