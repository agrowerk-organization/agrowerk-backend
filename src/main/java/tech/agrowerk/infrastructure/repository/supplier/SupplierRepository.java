package tech.agrowerk.infrastructure.repository.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.supplier.Supplier;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Optional<Supplier> findByAdministrator_Id(UUID administratorId);

    Optional<Supplier> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    Page<Supplier> findByAddress_MunicipalityContainingIgnoreCaseAndIsActiveTrue(String municipality, Pageable pageable);

}
