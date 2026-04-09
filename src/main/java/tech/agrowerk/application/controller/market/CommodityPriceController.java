package tech.agrowerk.application.controller.market;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.market.CommodityDashboardResponse;
import tech.agrowerk.application.dto.market.CommodityHistoryResponse;
import tech.agrowerk.application.dto.market.CommodityPriceResponse;
import tech.agrowerk.business.service.market.CommodityPriceService;
import tech.agrowerk.business.service.market.MarketDataScheduler;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

@RestController
@RequestMapping("/commodity-prices")
public class CommodityPriceController {

    private final CommodityPriceService commodityPriceService;
    private final MarketDataScheduler marketDataScheduler;

    public CommodityPriceController(CommodityPriceService commodityPriceService,
                                    MarketDataScheduler marketDataScheduler) {
        this.commodityPriceService = commodityPriceService;
        this.marketDataScheduler = marketDataScheduler;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommodityDashboardResponse> getDashboard() {
        return ResponseEntity.ok(commodityPriceService.getDashboard());
    }

    @GetMapping("/latest/{commodity}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommodityPriceResponse> getLatest(@PathVariable Commodity commodity) {
        return ResponseEntity.ok(commodityPriceService.getLatest(commodity));
    }

    @GetMapping("/history/{commodity}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommodityHistoryResponse> getHistory(
            @PathVariable Commodity commodity,
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(commodityPriceService.getHistory(commodity, days));
    }

    @PostMapping("/admin/sync-market")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> forceSyncMarket() {
        marketDataScheduler.forceSyncNow();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/backfill")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> initialBackfill() {
        marketDataScheduler.initialBackfill();
        return ResponseEntity.accepted().build();
    }
}
