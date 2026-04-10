package tech.agrowerk.infrastructure.model.market;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "exchange_rates",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "currency_pair", "reference_date"
                }
        )
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "currency_pair", length = 10, nullable = false)
    private String currencyPair;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal rate;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;
}
