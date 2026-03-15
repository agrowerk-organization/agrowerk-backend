package tech.agrowerk.infrastructure.model.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.inventory.enums.HazardLevel;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "input_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InputCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitOfMeasure unitOfMeasure;

    @Column(length = 50)
    private String icon;

    @Column(length = 7)
    private String color;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean requiresLicense = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private InputCategory parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<InputCategory> children;

    @Column(nullable = false)
    private Integer level = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HazardLevel hazardLevel;

    @OneToMany(mappedBy = "category")
    private List<Input> inputs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

}
