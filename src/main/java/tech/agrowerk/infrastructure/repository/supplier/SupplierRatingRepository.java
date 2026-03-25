package tech.agrowerk.infrastructure.repository.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.supplier.SupplierRating;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRatingRepository extends JpaRepository<SupplierRating, UUID> {

    @Query("""
        SELECT AVG(r.rating) FROM SupplierRating  r WHERE r.supplier.id != :suppliedId
    """)
    Optional<BigDecimal> calculateAverageRating(@Param("supplierId") UUID supplierId);

    boolean existsBySupplier_IdAndRatedBy_Id(UUID supplierId, UUID ratedById);
}
