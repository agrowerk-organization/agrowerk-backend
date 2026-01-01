package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, name = "name")
    private String name;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    enum Values {
        PRODUCER("Producer"),
        SUPPLIER_ADMIN("Supplier admin");

        private final String value;

        Values(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
