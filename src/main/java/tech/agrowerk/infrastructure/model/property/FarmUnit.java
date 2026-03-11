package tech.agrowerk.infrastructure.model.property;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tech.agrowerk.infrastructure.model.core.Address;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "farm_units")
@Getter
@Setter
public class FarmUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Address address;

    @Column(precision = 10, scale = 2)
    private BigDecimal area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;
}