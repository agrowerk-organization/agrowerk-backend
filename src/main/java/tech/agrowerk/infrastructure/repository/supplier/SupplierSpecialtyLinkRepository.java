package tech.agrowerk.infrastructure.repository.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.supplier.SupplierSpecialtyLink;

@Repository
public interface SupplierSpecialtyLinkRepository extends JpaRepository<SupplierSpecialtyLink, Long> {
}
