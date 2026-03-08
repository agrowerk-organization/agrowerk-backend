package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.AgriculturalPractice;
import tech.agrowerk.infrastructure.model.farming.enums.PractipeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgriculturalPracticeRepository extends JpaRepository<AgriculturalPractice, UUID> {

    Page<AgriculturalPractice> findByPlanting_Id(UUID plantingId, Pageable pageable);

    List<AgriculturalPractice> findByPlanting_IdAndPractipeType(UUID plantingId, PractipeType practipeType);

    @Query("""
            SELECT COALESCE(SUM(ap.costAmount), 0) FROM AgriculturalPractice ap
            WHERE ap.planting.id = :plantingId
    """)
    BigDecimal sumCostByPlanting(@Param("plantingId") UUID plantingId);

    @Query("""
        SELECT COALESCE(SUM(ap.costAmount), 0) FROM AgriculturalPractice ap
        WHERE ap.planting.property.id = :propertyId
        AND ap.applicationDate BETWEEN :start AND :end
    """)
    BigDecimal sumCostByPropertyAndPeriod(
            @Param("propertyId") UUID propertyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
