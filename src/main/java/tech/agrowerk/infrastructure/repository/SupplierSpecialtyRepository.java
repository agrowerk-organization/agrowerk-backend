package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.SupplierSpecialty;

@Repository
public interface SupplierSpecialtyRepository extends JpaRepository<SupplierSpecialty, Long> {
}
