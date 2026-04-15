package tech.agrowerk.infrastructure.model.market.enums;

public enum AlertType {

    PRICE_SPIKE    ("Alta de preço",       AlertSeverity.WARNING),
    PRICE_DROP     ("Queda de preço",      AlertSeverity.WARNING),
    HIGH_VOLATILITY("Alta volatilidade",   AlertSeverity.INFO),
    EXCHANGE_SURGE ("Alta do dólar",       AlertSeverity.WARNING),
    EXCHANGE_DROP("Baixa do dólar", AlertSeverity.OPPORTUNITY),
    SELL_WINDOW    ("Janela de venda",     AlertSeverity.OPPORTUNITY),
    BUY_WINDOW     ("Janela de compra",    AlertSeverity.OPPORTUNITY);

    private final String displayName;
    private final AlertSeverity severity;

    AlertType(String displayName, AlertSeverity severity) {
        this.displayName = displayName;
        this.severity = severity;
    }
}

