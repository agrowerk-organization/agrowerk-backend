package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.infrastructure.enums.RoleType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 15)
    private String telephone;

    @Column(unique = true, length = 14)
    @CPF
    private String cpf;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime lastLogin;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_property",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "property_id")
    )
    private Set<Property> properties;

    @OneToOne(mappedBy = "administrator")
    private Supplier supplier;

    @OneToMany(mappedBy = "user")
    private List<StockMovement> movements;

    @Column(nullable = false, columnDefinition = "boolean default = false")
    private boolean isDeleted = false;

    public boolean isLoginCorrect(LoginRequest loginRequest, BCryptPasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(loginRequest.password(), this.password);
    }

    public boolean isProducer() {
        return role != null && role.getName() == RoleType.PRODUCER;
    }

    public boolean isSupplierAdmin() {
        return role != null && role.getName() == RoleType.SUPPLIER_ADMIN;
    }
}
