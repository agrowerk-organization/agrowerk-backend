package tech.agrowerk.infrastructure.model.barter.enums;

public enum OfferStatus {
    ACTIVE("Disponível"),
    ACCEPTED("Aceita"),
    COMPLETED("Encerrada"),
    CANCELLED("Cancelada"),
    EXPIRED("Expirada");

    private final String description;

    OfferStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
