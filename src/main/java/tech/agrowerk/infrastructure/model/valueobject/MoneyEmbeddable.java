package tech.agrowerk.infrastructure.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import tech.agrowerk.infrastructure.model.valueobject.enums.Currency;

import java.math.BigDecimal;

@Embeddable
public class MoneyEmbeddable {

    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private Currency currency;

    protected MoneyEmbeddable() {
    }

    public MoneyEmbeddable(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static MoneyEmbeddable from(Money money) {
        return new MoneyEmbeddable(money.getAmount(), money.getCurrency());
    }

    public Money toMoney() {
        return Money.of(amount, currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}