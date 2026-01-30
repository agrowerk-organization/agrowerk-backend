package tech.agrowerk.infrastructure.model.shared_enums;

public enum UnitOfMeasure {
    KILOGRAM("kg", "Kilogram"),
    LITER("L", "Liter"),
    BAG("bag", "Bag"),
    UNIT("un", "Unit"),
    TON("t", "Ton"),
    MILLILITER("ml", "Milliliter"),
    GRAM("g", "Gram");

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