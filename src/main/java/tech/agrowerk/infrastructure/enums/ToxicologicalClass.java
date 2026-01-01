package tech.agrowerk.infrastructure.enums;

public enum ToxicologicalClass {
    CLASS_I("Extremely Toxic", "red"),
    CLASS_II("Highly Toxic", "orange"),
    CLASS_III("Moderately Toxic", "yellow"),
    CLASS_IV("Slightly Toxic", "blue"),
    NOT_CLASSIFIED("Not Classified", "gray");

    private final String description;
    private final String color;

    ToxicologicalClass(String description, String color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }
}