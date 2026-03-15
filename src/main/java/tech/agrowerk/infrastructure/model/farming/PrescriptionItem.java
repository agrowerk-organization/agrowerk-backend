package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "prescription_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"prescription_id", "input_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private AgronomicPrescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", nullable = false)
    private Input input;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal authorizedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitOfMeasure unit;

    @Column(columnDefinition = "TEXT")
    private String usageInstructions;
}