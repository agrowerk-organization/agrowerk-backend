package tech.agrowerk.infrastructure.model.inventory.enums;

public enum MovementType {
    ENTRY("Entry", true),
    EXIT("Exit", false),
    TRANSFER_OUT("Transfer - Out", false),
    TRANSFER_IN("Transfer - In", true),
    POSITIVE_ADJUSTMENT("Positive adjustment", true),
    NEGATIVE_ADJUSTMENT("Negative adjustment", false),
    LOSS("Loss", false),
    RETURN("Return", true);

    private final String description;
    private final boolean increasesStock;

    private MovementType(final String description, final boolean increasesStock) {
        this.description = description;
        this.increasesStock = increasesStock;
    }

    public String getDescription() {
        return description;
    }

    public boolean isIncreasesStock() {
        return increasesStock;
    }
}
