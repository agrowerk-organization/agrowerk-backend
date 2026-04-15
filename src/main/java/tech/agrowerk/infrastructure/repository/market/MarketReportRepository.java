package tech.agrowerk.infrastructure.repository.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.market.MarketReport;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketReportRepository extends JpaRepository<MarketReport, UUID> {
    Optional<MarketReport> findTopByReportTypeOrderByGeneratedAtDesc(ReportType reportType);
}
