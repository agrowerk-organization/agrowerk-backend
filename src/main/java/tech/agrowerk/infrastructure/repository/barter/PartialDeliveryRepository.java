package tech.agrowerk.infrastructure.repository.barter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.PartialDelivery;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PartialDeliveryRepository extends JpaRepository<PartialDelivery, UUID> {

    List<PartialDelivery> findByCommitment_IdOrderByDeliveryDateDesc(UUID commitmentId);

    @Query("""
        SELECT COALESCE(SUM(p.deliveredQuantity), 0)
        FROM PartialDelivery p 
        WHERE p.commitment.id = :commitmentId
    """)
    BigDecimal sumDeliveredByCommitment(@Param("commitmentId") UUID commitmentId);
}
