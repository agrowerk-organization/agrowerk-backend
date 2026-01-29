package tech.agrowerk.infrastructure.model.support.enums;

public enum TicketCategory {
    INPUT("Insumos", "Dúvidas sobre insumos, dosagens e aplicações."),
    SUPPLIER("Fornecedores", "Questões sobre fornecedores, relações comerciais."),
    CROP("Culturas e safras", "Dúvidas sobre plantio, colheita e gestão de safras."),
    STOCK("Estoque", "Problemas com controle de estoque e movimentações."),
    PROPERTY("Propriedades", "Questões sobre cadastro e gestão de propriedades."),
    TECHNICAL("Problemas técnicos", "Bugs, erros no sistema e problemas de acesso."),
    FEATURE_REQUEST("", ""),
    OTHER("Outros", "Outras questões.");

    private final String displayName;
    private final String description;

    TicketCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
