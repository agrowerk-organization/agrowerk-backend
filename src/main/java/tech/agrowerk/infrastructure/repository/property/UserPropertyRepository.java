package tech.agrowerk.infrastructure.repository.property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.property.UserProperty;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPropertyRepository extends JpaRepository<UserProperty, UUID> {

    boolean existsByPropertyIdAndUserIdAndIsActiveTrue(UUID propertyId, UUID userId);

    Optional<UserProperty> findByPropertyIdAndUserIdAndIsActiveTrue(UUID propertyId, UUID userId);

    boolean existsByUserIdAndPropertyId(UUID userId, UUID propertyId);

    @Query("""
        SELECT COUNT(up) > 0 FROM UserProperty up
        JOIN Batch b ON b.property.id = up.id
        WHERE up.user.id = :userId
        AND b.supplier.id = :supplierId
        AND up.isActive = true
    """)
    boolean hasUserPurchasedFromSupplier(@Param("userId") UUID userId, @Param("supplierId") UUID supplierId);
}