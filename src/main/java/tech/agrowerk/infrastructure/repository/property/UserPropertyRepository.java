package tech.agrowerk.infrastructure.repository.property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.property.UserProperty;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPropertyRepository extends JpaRepository<UserProperty, UUID> {

    boolean existsByProperty_IdAndUser_IdAndIsActiveTrue(UUID propertyId, UUID userId);

    Optional<UserProperty> findByProperty_IdAndUser_IdAndIsActiveTrue(UUID propertyId, UUID userId);
}
