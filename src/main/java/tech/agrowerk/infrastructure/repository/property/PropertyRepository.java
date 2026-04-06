package tech.agrowerk.infrastructure.repository.property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.property.Property;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    boolean existsByStateRegistration(String stateRegistration);

    Page<Property> findByUserLinksUserIdAndUserLinksIsActiveTrue(UUID userId, Pageable pageable);

    @Query("SELECT p FROM Property p " +
            "LEFT JOIN FETCH p.units " +
            "LEFT JOIN FETCH p.address " +
            "WHERE p.id = :id")
    Optional<Property> findByIdWithUnits(@Param("id") UUID id);

}
