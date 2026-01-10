package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.agrowerk.infrastructure.enums.CropCategory;

import java.util.Set;

@Entity
@Table(name = "crops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String scientificName;

    private int growthCycleDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropCategory cropCategory;

    @ManyToMany(mappedBy = "crops")
    private Set<Supplier> suppliers;
}
