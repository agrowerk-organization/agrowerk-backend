package tech.agrowerk.infrastructure.model.core;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.core.enums.RoleType;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, name = "name")
    private RoleType name;

    @OneToMany(mappedBy = "role")
    private List<User> users;
}
