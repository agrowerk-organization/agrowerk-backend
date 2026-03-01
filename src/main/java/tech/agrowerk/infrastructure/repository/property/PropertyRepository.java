package tech.agrowerk.infrastructure.repository.property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.property.Property;

import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    boolean existsByStateRegistration(String stateRegistration);
    Page<Property> findByUserLinksUserIdAndUserLinksIsActiveTrue(UUID userId, Pageable pageable);}
