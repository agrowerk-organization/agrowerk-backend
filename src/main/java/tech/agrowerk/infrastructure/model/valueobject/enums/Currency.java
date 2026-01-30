package tech.agrowerk.infrastructure.model.valueobject.enums;

import java.util.Locale;

public enum Currency {
    USD("US Dollar"),
    BRL("Brazilian Real"),
    EUR("Euro");

    private final String displayName;

    Currency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return java.util.Currency.getInstance(this.name()).getSymbol(Locale.US);
    }

    public int getDecimalPlaces() {
        return java.util.Currency.getInstance(this.name()).getDefaultFractionDigits();
    }

    public static Currency fromCode(String code) {
        try {
            return Currency.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Unsupported currency code: " + code);
        }
    }
}