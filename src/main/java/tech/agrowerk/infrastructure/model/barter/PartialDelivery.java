package tech.agrowerk.infrastructure.model.barter;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.file.FileMetadata;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "partial_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartialDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commitment_id", nullable = false)
    private CropCommitment commitment;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveredQuantity;

    @Column(nullable = false)
    private LocalDate deliveryDate;

    @Column(precision = 5, scale = 2)
    private BigDecimal moisturePercentage;

    @Column(precision = 5, scale = 2)
    private BigDecimal impurityPercentage;

    @Column(length = 50)
    private String qualityGrade;

    @ManyToMany
    @JoinTable(
            name = "partial_delivery_documents",
            joinColumns = @JoinColumn(name = "delivery_id"),
            inverseJoinColumns = @JoinColumn(name = "file_metadata_id")
    )
    private List<FileMetadata> receiptDocuments;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

}