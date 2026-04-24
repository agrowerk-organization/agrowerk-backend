package tech.agrowerk.infrastructure.model.barter.enums;

public enum DeliveryStatus {
    SCHEDULED("Agendada"),
    IN_TRANSIT("Em Trânsito"),
    DELIVERED("Entregue"),
    FAILED("Falha na Entrega"),
    CANCELLED("Cancelada");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
