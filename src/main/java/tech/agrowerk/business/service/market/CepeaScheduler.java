package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.market.CepeaPrice;
import tech.agrowerk.infrastructure.client.CepeaClient;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CepeaScheduler {

    private final CepeaClient cepeaClient;
    private final CommodityPriceService commodityPriceService;

    public CepeaScheduler(CepeaClient cepeaClient, CommodityPriceService commodityPriceService) {
        this.cepeaClient = cepeaClient;
        this.commodityPriceService = commodityPriceService;
    }

    @Scheduled(cron = "${cepea.scheduler.cron:0 30 18 * * MON-FRI}")
    public void syncAllCommodities() {
        log.info("Starting CEPEA sync...");

        sync(cepeaClient.fetchSoja(),      Commodity.SOJA);
        sync(cepeaClient.fetchMilho(),     Commodity.MILHO);
        sync(cepeaClient.fetchBoiGordo(),  Commodity.BOI_GORDO);
        sync(cepeaClient.fetchCafe(),      Commodity.CAFE);
        sync(cepeaClient.fetchAlgodao(),   Commodity.ALGODAO);

        log.info("CEPEA sync complete.");
    }

    private void sync(List<CepeaPrice> cepeaPrices, Commodity commodity) {
        int saved = 0;
        for (CepeaPrice cepeaPrice: cepeaPrices) {
            try {
                CommodityPrice price = CommodityPrice.builder()
                        .commodity(commodity)
                        .price(cepeaPrice.price())
                        .unit(cepeaPrice.unit())
                        .region(cepeaPrice.region())
                        .referenceDate(cepeaPrice.referenceDate())
                        .fetchedAt(LocalDateTime.now())
                        .build();

                commodityPriceService.saveIfNotExists(price);
                saved++;
            } catch (Exception e) {
                log.warn("Skipping record for {} on {}: {}",
                        commodity, cepeaPrice.referenceDate(), e.getMessage());
            }
        }
        log.info("Synced {}/{} records for {}", saved, records.size(), commodity);
    }
}
