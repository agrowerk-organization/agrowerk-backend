package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.business.service.market.strategy.ReportGenerator;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.ExchangeRate;
import tech.agrowerk.infrastructure.model.market.MarketReport;
import tech.agrowerk.infrastructure.model.market.ReportPayload;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;
import tech.agrowerk.infrastructure.model.market.enums.ReportStatus;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;
import tech.agrowerk.infrastructure.repository.market.CommodityPriceRepository;
import tech.agrowerk.infrastructure.repository.market.ExchangeRateRepository;
import tech.agrowerk.infrastructure.repository.market.MarketReportRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketReportService {

    private final Map<ReportType, ReportGenerator> generators;
    private final MarketReportRepository marketReportRepository;

    public MarketReportService(List<ReportGenerator> generators,
                               MarketReportRepository marketReportRepository) {

        this.generators = generators.stream()
                .collect(Collectors.toMap(ReportGenerator::getType, g -> g));

        this.marketReportRepository = marketReportRepository;
    }

    public MarketReport generate(ReportType type) {
        ReportGenerator generator = generators.get(type);

        if (generator == null) {
            throw new IllegalArgumentException("Tipo não suportado: " + type);
        }

        return generator.generate();
    }

    public Optional<MarketReport> getLatestByType(ReportType type) {
        return marketReportRepository
                .findTopByReportTypeOrderByGeneratedAtDesc(type);
    }
}