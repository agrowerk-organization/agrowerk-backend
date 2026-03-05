package tech.agrowerk.infrastructure.model.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "input_crops", indexes = {
        @Index(name = "idx_input_crop_input", columnList = "input_id"),
        @Index(name = "idx_input_crop_crop", columnList = "crop_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"input_id", "crop_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InputCrop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", nullable = false)
    private Input input;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(columnDefinition = "TEXT")
    private String usageRecommendation;

    @Column(precision = 10, scale = 3)
    private BigDecimal recommendedDosePerHectare;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UnitOfMeasure doseUnit;

    @Column(nullable = false)
    private Boolean approvedByAdmin = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column
    private Instant approvedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}