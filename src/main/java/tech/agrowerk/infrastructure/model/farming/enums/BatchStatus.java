package tech.agrowerk.infrastructure.model.farming.enums;

public enum BatchStatus {
    AVAILABLE("Available for use"),
    IN_USE("In use"),
    DEPLETED("Depleted"),
    EXPIRED("Expired"),
    BLOCKED("Blocked");


    private final String description;

    BatchStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
