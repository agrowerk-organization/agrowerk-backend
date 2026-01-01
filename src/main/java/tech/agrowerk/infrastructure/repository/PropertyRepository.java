package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
}
