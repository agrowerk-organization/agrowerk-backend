package tech.agrowerk.infrastructure.enums;

public enum SupportTicketPriority {
    LOW("Baixo"),
    MEDIUM("Médio"),
    HIGH("Alto"),
    URGENT("Urgente");

    private final String displayName;

    SupportTicketPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
