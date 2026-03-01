package tech.agrowerk.infrastructure.model.property;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.property.enums.OwnerRemovalReason;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_property")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private boolean isMasterOwner = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "can_edit", nullable = false)
    private boolean canEdit = false;

    @Column(name = "can_edit_granted_at")
    private Instant canEditGrantedAt;

    @Column(name = "can_edit_granted_by")
    private UUID canEditGrantedBy;

    @Column(name = "removed_at")
    private Instant removedAt;

    @Column(name = "removed_by")
    private UUID removedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "removal_reason")
    private OwnerRemovalReason removalReason;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
