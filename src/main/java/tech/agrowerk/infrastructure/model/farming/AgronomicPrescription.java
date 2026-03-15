package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agronomic_prescriptions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AgronomicPrescription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planting_id", nullable = false)
    private Planting planting;

    @Column(nullable = false, length = 255)
    private String agronomistName;

    @Column(nullable = false, length = 20)
    private String agronomistCrea;

    @Column(nullable = false)
    private LocalDate issuedAt;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false, length = 500)
    private String documentUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "prescription",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PrescriptionItem> items;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public boolean isValid() {
        return active && LocalDate.now().isBefore(validUntil);
    }
}