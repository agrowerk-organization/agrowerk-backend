package tech.agrowerk.business.service.market.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.market.MarketPrice;
import tech.agrowerk.business.service.market.CommodityPriceService;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.client.MarketDataClient;

import java.util.List;

@Service
@Slf4j
public class MarketDataScheduler {

    private final MarketDataClient marketDataClient;
    private final CommodityPriceService commodityPriceService;
    private final AuthUtil authUtil;

    public MarketDataScheduler(MarketDataClient marketDataClient,
                               CommodityPriceService commodityPriceService,
                               AuthUtil authUtil) {
        this.marketDataClient = marketDataClient;
        this.commodityPriceService = commodityPriceService;
        this.authUtil = authUtil;
    }

    @Scheduled(cron = "${market.scheduler.cron:0 0 6 1-7 * MON}", zone = "America/Fortaleza")
    public void syncMarketData() {
        log.info("Starting market data sync...");

        try {
            List<MarketPrice> marketPrices = marketDataClient.fetchAll();

            int saved = 0;

            for (MarketPrice marketPrice: marketPrices) {
                try {
                    commodityPriceService.saveFromMarketRecord(marketPrice);
                    saved++;
                } catch (Exception e) {
                    log.warn("Failed to save {}: {}", marketPrice.commodity(), e.getMessage());
                }
            }

            log.info("Market data sync complete. Saved {}/{} market prices", saved, marketPrices.size());

        } catch (Exception e) {
            log.error("Market data sync failed: {}", e.getMessage());
        }
    }

    public void forceSyncNow() {
        log.info("Forced market data sync triggered");
        syncMarketData();
    }

    public void initialBackfill() {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        log.info("Initial backfill triggered by system admin: {}", auth);
        syncMarketData();
    }
}
