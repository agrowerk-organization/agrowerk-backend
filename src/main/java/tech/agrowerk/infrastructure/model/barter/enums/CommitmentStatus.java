package tech.agrowerk.infrastructure.model.barter.enums;

public enum CommitmentStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmado"),
    PARTIALLY_DELIVERED("Entrega Parcial"),
    DELIVERED("Totalmente Entregue"),
    OVERDUE("Atrasado"),
    CANCELLED("Cancelado");

    private final String description;

    CommitmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}