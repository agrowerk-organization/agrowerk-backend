package tech.agrowerk.infrastructure.model.supplier;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.validator.constraints.br.CNPJ;
import tech.agrowerk.infrastructure.model.core.Address;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Batch;
import tech.agrowerk.infrastructure.model.farming.Crop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    private List<SupplierRating> ratings;

    @OneToMany(mappedBy = "supplier", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<SupplierSpecialtyLink> specialties;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean acceptsBarterDeals = false;

    @Column(columnDefinition = "TEXT")
    private String barterTerms;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id", unique = true, nullable = false)
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

    public BigDecimal getAverageRating() {
        if (ratings == null || ratings.isEmpty()) return BigDecimal.ZERO;

        return ratings.stream()
                .map(SupplierRating::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP);
    }
}
