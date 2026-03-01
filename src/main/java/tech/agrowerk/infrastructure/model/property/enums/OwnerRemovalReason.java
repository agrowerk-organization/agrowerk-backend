package tech.agrowerk.infrastructure.model.property.enums;

public enum OwnerRemovalReason {
    VOLUNTARY_EXIT("Saída voluntária"),
    REMOVED_BY_MASTER("Removido pelo dono master"),
    PROPERTY_TRANSFERRED("Transferido de propriedade"),
    OTHER("Outro motivo");

    private final String description;

    OwnerRemovalReason(String description) {
        this.description = description;
    }
}
