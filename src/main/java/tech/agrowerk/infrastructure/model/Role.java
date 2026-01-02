package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.enums.RoleType;

import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, name = "name")
    private RoleType name;

    @OneToMany(mappedBy = "role")
    private List<User> users;
}
