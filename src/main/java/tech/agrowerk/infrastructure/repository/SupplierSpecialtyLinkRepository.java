package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.SupplierSpecialtyLink;

@Repository
public interface SupplierSpecialtyLinkRepository extends JpaRepository<SupplierSpecialtyLink, Long> {
}
