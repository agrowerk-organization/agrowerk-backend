package tech.agrowerk.business.service.market.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.agrowerk.business.service.market.MarketAlertService;

@Component
@Slf4j
public class MarketAlertScheduler {

    private final MarketAlertService marketAlertService;

    public MarketAlertScheduler(MarketAlertService marketAlertService) {
        this.marketAlertService = marketAlertService;
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void runEvaluation() {
        log.info("Running market alert scheduler...");
        marketAlertService.evaluateAll();
    }
}
