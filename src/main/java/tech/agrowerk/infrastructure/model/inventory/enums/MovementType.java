package tech.agrowerk.infrastructure.model.inventory.enums;

public enum MovementType {
    PURCHASE("Purchase", true),
    BARTER_IN("Barter - In", true),
    TRANSFER_IN("Transfer - In", true),
    HARVEST_IN("Harvest - In", true),
    INITIAL_BALANCE("Initial Balance", true),
    PLANTING_USE("Planting Use", false),
    BARTER_OUT("Barter - Out", false),
    TRANSFER_OUT("Transfer - Out", false),
    LOSS("Loss", false),
    RETURN("Return", false),
    POSITIVE_ADJUSTMENT("Positive Adjustment", true),
    NEGATIVE_ADJUSTMENT("Negative Adjustment", false),
    REVERSAL("Reversal", true);

    private final String description;
    private final boolean increasesStock;

    MovementType(String description, boolean increasesStock) {
        this.description = description;
        this.increasesStock = increasesStock;
    }

    public String getDescription() { return description; }
    public boolean isIncreasesStock() { return increasesStock; }
}