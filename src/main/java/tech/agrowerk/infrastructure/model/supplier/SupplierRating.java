package tech.agrowerk.infrastructure.model.supplier;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tech.agrowerk.infrastructure.model.core.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "supplier_ratings", indexes = {
        @Index(name = "idx_rating_supplier", columnList = "supplier_id"),
        @Index(name = "idx_rating_user", columnList = "rated_by_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"supplier_id", "rated_by_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_by_id", nullable = false)
    private User ratedBy;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}