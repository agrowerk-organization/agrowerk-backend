package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tech.agrowerk.infrastructure.model.core.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "harvest_partials", indexes = {
        @Index(name = "idx_partial_harvest", columnList = "harvest_id"),
        @Index(name = "idx_partial_date", columnList = "partial_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HarvestPartial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_id", nullable = false)
    private Harvest harvest;

    @Column(nullable = false)
    private LocalDate partialDate;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityKg;

    @Column(length = 50)
    private String qualityGrade;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id", nullable = false)
    private User responsibleUser;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}