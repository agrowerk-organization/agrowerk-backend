package tech.agrowerk.infrastructure.model.barter.enums;

public enum TransactionStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmada"),
    IN_PROGRESS("Em Processamento"),
    COMPLETED("Concluída"),
    CANCELLED("Cancelada"),
    DISPUTED("Em Disputa/ Análise");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}