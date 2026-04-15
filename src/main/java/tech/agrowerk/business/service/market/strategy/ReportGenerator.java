package tech.agrowerk.business.service.market.strategy;

import tech.agrowerk.infrastructure.model.market.MarketReport;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;

public interface ReportGenerator {
    ReportType getType();
    MarketReport generate();
}
