package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String corporateReason;

    @Column(length = 255)
    private String fantasyName;

    @CNPJ
    @Column(nullable = false, length = 18, unique = true)
    private String cnpj;

    @Column(length = 255)
    private String stateRegistration;

    @Column(length = 255, nullable = false)
    private String email;

    @Column(length = 15)
    private String telephone;

    @Column(length = 255)
    private String nameContact;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierSpecialtyLink> specialties;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(nullable = false)
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "administrator_id", unique = true)
    private User administrator;

    @OneToMany(mappedBy = "supplier")
    private List<Batch> batches;

    @ManyToMany
    @JoinTable(
            name = "supplier_crops",
            joinColumns = @JoinColumn(name = "supplier_id"),
            inverseJoinColumns = @JoinColumn(name = "crop_id")
    )
    private Set<Crop> crops;
}
