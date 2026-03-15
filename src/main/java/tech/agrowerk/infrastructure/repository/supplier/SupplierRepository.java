package tech.agrowerk.infrastructure.repository.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.supplier.Supplier;

import java.lang.ScopedValue;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Optional<Supplier> findByAdministrator_Id(UUID administratorId);
}
