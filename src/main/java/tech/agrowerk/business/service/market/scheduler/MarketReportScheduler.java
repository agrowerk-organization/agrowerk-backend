package tech.agrowerk.business.service.market.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.agrowerk.business.service.market.MarketReportService;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketReportScheduler {

    private final MarketReportService marketReportService;

    @Scheduled(cron = "0 0 7 1 * *")
    public void generateMonthly() {
        log.info("Generating monthly report...");
        marketReportService.generate(ReportType.MONTHLY_TREND);
    }

    @Scheduled(cron = "0 0 0 3 1 *")
    public void generateAnnualy() {
        log.info("Generating annualy report...");
        marketReportService.generate(ReportType.ANNUAL_TREND);
    }


    @Scheduled(cron = "0 0 8 * * *")
    public void generateVolatility() {
        log.info("Generating a volatility report...");
        marketReportService.generate(ReportType.VOLATILITY_ALERT);
    }
}
