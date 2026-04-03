package tech.agrowerk.infrastructure.model.market;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "commodity_prices",
        indexes = {
            @Index(name = "idx_commodity_date", columnList = "commodity, reference_date DESC"),
            @Index(name = "idx_reference_date", columnList = "reference_date DESC"),
            @Index(name = "idx_commodity_date", columnList = "commodity, reference_date DESC")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_commodity_region_date",
                    columnNames = {"commodity", "reference_date"}
            )
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CommodityPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Commodity commodity;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal priceUsd;

    @Column(precision = 8, scale = 4)
    private BigDecimal exchangeRate;

    @Column(length = 30)
    private String unit;

    @Column(length = 20)
    private String source;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;
}
