package tech.agrowerk.infrastructure.model.supplier;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.br.CNPJ;
import tech.agrowerk.infrastructure.model.core.Address;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Batch;
import tech.agrowerk.infrastructure.model.farming.Crop;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @OneToMany(mappedBy = "supplier", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<SupplierSpecialtyLink> specialties;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean acceptsBarterDeals = false;

    @Column(columnDefinition = "TEXT")
    private String barterTerms;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id", unique = true)
    private User administrator;

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    private List<Batch> batches;

    @ManyToMany
    @JoinTable(
            name = "supplier_crops",
            joinColumns = @JoinColumn(name = "supplier_id"),
            inverseJoinColumns = @JoinColumn(name = "crop_id")
    )
    private Set<Crop> crops;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
