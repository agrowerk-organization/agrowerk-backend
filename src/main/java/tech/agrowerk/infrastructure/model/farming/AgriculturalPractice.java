package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.enums.PractipeType;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "agricultural_practices", indexes = {
        @Index(name = "idx_practice_planting", columnList = "planting_id"),
        @Index(name = "idx_practice_type", columnList = "practipe_type"),
        @Index(name = "idx_practice_date", columnList = "application_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgriculturalPractice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PractipeType practipeType;

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Column(length = 200)
    private String productUsed;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantityUsed;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UnitOfMeasure unitOfMeasure;

    @Column(precision = 10, scale = 2)
    private BigDecimal costAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planting_id", nullable = false)
    private Planting planting;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
