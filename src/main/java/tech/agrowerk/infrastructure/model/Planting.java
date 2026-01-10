package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.agrowerk.infrastructure.enums.PlantingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "plantings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Planting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal areaHectares;

    @Column(nullable = false)
    private LocalDate plantingDate;

    @Column(nullable = false)
    private LocalDate expectedHarvestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlantingStatus plantingStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @OneToMany(mappedBy = "planting")
    private List<PlantingInput> plantingInputs;

}
