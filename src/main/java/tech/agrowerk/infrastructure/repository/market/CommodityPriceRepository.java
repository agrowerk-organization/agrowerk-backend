package tech.agrowerk.infrastructure.repository.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommodityPriceRepository extends JpaRepository<CommodityPrice, UUID> {

    Optional<CommodityPrice> findTopByCommodityOrderByReferenceDateDesc(Commodity commodity);

    List<CommodityPrice> findByCommodityAndReferenceDateBetweenOrderByReferenceDateDesc(
            Commodity commodity, LocalDate start, LocalDate end);

    List<CommodityPrice> findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
            List<Commodity> commodities, LocalDate from, LocalDate to
    );

    Optional<CommodityPrice> findFirstByCommodityAndReferenceDateBeforeOrderByReferenceDateDesc(
            Commodity commodity, LocalDate date);

    boolean existsByCommodityAndSourceAndReferenceDate(Commodity commodity, String source, LocalDate referenceDate);


    @Query("""
        SELECT cp FROM CommodityPrice cp
        WHERE cp.referenceDate = (
            SELECT MAX(cp2.referenceDate) FROM CommodityPrice cp2
            WHERE cp2.commodity = cp.commodity
        )
    """)
    List<CommodityPrice> findLatestPricePerCommodity();


    @Query("""
        SELECT cp FROM CommodityPrice cp
        WHERE cp.commodity = :commodity
        AND cp.referenceDate >= :since
        ORDER BY cp.referenceDate DESC
    """)
    List<CommodityPrice> findRecentByCommodity(
            @Param("commodity") Commodity commodity,
            @Param("since") LocalDate since);
}
