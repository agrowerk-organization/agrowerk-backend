package tech.agrowerk.infrastructure.repository.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.supplier.SupplierRating;
import tech.agrowerk.infrastructure.model.supplier.SupplierSpecialtyLink;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierSpecialtyLinkRepository extends JpaRepository<SupplierSpecialtyLink, UUID> {

    Optional<SupplierSpecialtyLink> findBySupplier_IdAndSpecialty_Id(UUID supplierId, UUID specialtyId);

}
