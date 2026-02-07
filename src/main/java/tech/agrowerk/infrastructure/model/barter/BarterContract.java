package tech.agrowerk.infrastructure.model.barter;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.barter.enums.ContractStatus;
import tech.agrowerk.infrastructure.model.file.FileMetadata;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "barter_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarterContract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private BarterTransaction transaction;

    @Column(nullable = false, unique = true, length = 50)
    private String contractNumber;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus contractStatus;

    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    @ManyToMany
    @JoinTable(
            name = "contract_documents",
            joinColumns = @JoinColumn(name = "contract_id"),
            inverseJoinColumns = @JoinColumn(name = "file_metadata_id")
    )
    private List<FileMetadata> attachedDocuments;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (contractStatus == null) {
            contractStatus = ContractStatus.DRAFT;
        }
    }
}