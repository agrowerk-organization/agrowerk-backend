package tech.agrowerk.infrastructure.repository.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.supplier.SupplierSpecialty;

@Repository
public interface SupplierSpecialtyRepository extends JpaRepository<SupplierSpecialty, Long> {
}
