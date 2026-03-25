package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.market.MarketPrice;
import tech.agrowerk.infrastructure.client.MarketDataClient;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MarketDataScheduler {

    private final MarketDataClient marketDataClient;
    private final CommodityPriceService commodityPriceService;

    public MarketDataScheduler(MarketDataClient marketDataClient, CommodityPriceService commodityPriceService) {
        this.marketDataClient = marketDataClient;
        this.commodityPriceService = commodityPriceService;
    }

    @Scheduled(cron = "${market.scheduler.cron:0 30 18 * * MON-FRI}")
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

}
