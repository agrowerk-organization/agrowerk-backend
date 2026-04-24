package tech.agrowerk.infrastructure.model.barter.enums;

public enum ContractStatus {
    DRAFT("Rascunho"),
    AWAITING_OFFEROR_SIGNATURE("Aguardando Assinatura do Ofertante"),
    AWAITING_ACCEPTOR_SIGNATURE("Aguardando Assinatura do Aceitante"),
    ACTIVE("Ativo / Em Vigência"),
    COMPLETED("Finalizado"),
    CANCELLED("Cancelado"),
    EXPIRED("Expirado");

    private final String description;

    ContractStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}