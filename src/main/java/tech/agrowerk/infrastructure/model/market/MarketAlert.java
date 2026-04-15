package tech.agrowerk.infrastructure.model.market;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.market.enums.AlertType;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "market_alerts", indexes = {
        @Index(name = "idx_alert_commodity_date", columnList = "commodity, reference_date"),
        @Index(name = "idx_alert_read", columnList = "read")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarketAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Commodity commodity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Column(nullable = false)
    private String message;

    @Column(precision = 10, scale = 4)
    private BigDecimal triggerValue;

    @Column(nullable = false)
    private LocalDate referenceDate;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private Instant createdAt;

    public void markRead() {
        this.read = true;
    }
}