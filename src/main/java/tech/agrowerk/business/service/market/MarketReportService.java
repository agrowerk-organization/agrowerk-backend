package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.business.service.market.strategy.ReportGenerator;
import tech.agrowerk.infrastructure.model.market.MarketReport;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;
import tech.agrowerk.infrastructure.repository.market.MarketReportRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MarketReportService {

    private final Map<ReportType, ReportGenerator> generators;
    private final MarketReportRepository marketReportRepository;

    public MarketReportService(List<ReportGenerator> generators,
                               MarketReportRepository marketReportRepository) {
        this.generators = generators.stream()
                .collect(Collectors.toMap(ReportGenerator::getType, g -> g));
        this.marketReportRepository = marketReportRepository;
    }

    @CacheEvict(value = "marketReports", key = "#type")
    @Transactional
    public MarketReport generate(ReportType type) {
        log.info("Processing generation request for report type: {}", type);

        ReportGenerator generator = generators.get(type);

        if (generator == null) {
            log.error("Critical Failure: No generator found for report type {}", type);
            throw new IllegalArgumentException("Unsupported report type: " + type);
        }

        try {
            MarketReport report = generator.generate();
            log.info("Report {} successfully generated with ID: {}", type, report.getId());
            return report;

        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation while persisting report {}. Details: {}", type, e.getMessage());
            throw new DataIntegrityViolationException("Data integrity conflict for report type: " + type);

        } catch (Exception e) {
            log.error("Unexpected error during market report generation for {}: ", type, e);
            throw new RuntimeException("Internal error processing market report.");
        }
    }

    @Cacheable(value = "marketReports", key = "#type", unless = "#result == null")
    public Optional<MarketReport> getLatestByType(ReportType type) {
        log.debug("Fetching latest report for type: {}", type);
        return marketReportRepository
                .findTopByReportTypeOrderByGeneratedAtDesc(type);
    }
}