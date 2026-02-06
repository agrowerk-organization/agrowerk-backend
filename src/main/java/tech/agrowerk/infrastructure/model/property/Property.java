package tech.agrowerk.infrastructure.model.property;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.core.Address;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.inventory.Warehouse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 18)
    private String stateRegistration;

    @Column(length = 20)
    private String ruralRegistration;

    @Embedded
    private Address address;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalArea;

    @Column(precision = 10, scale = 2)
    private BigDecimal plantedArea;

    private String mainCrop;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToMany(mappedBy = "properties", fetch = FetchType.LAZY)
    private Set<User> users;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
    private List<StockMovement> movements;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
    private List<Planting> plantings;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
    private List<Warehouse> warehouses;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}