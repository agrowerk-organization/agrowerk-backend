package tech.agrowerk.infrastructure.repository.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.market.MarketAlert;
import tech.agrowerk.infrastructure.model.market.enums.AlertType;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MarketAlertRepository extends JpaRepository<MarketAlert, UUID> {

    List<MarketAlert> findByReadFalseOrderByCreatedAtDesc();

    boolean existsByCommodityAndTypeAndReferenceDate(
            Commodity commodity, AlertType type, LocalDate referenceDate);

    List<MarketAlert> findByCommodityAndCreatedAtAfter(
            Commodity commodity, LocalDateTime since);

    long countByReadFalse();
}
