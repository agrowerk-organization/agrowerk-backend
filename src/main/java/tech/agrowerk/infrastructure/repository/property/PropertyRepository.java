package tech.agrowerk.infrastructure.repository.property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.property.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
}
