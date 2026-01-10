package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movement_stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitValue;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Column(length = 255)
    private String destination;

    @Column(length = 255)
    private String crop;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(length = 50)
    private String documentNumber;

    @Column(nullable = false)
    private LocalDateTime movementDate;

    @Column
    private Long reversedMovementId;

    @Column(nullable = false)
    private Boolean reversed = false;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType movementType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @OneToOne
    @JoinColumn(name = "harvest_id")
    private Harvest harvest;
}
