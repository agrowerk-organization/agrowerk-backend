package tech.agrowerk.application.controller.market;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.market.UnreadCountResponse;
import tech.agrowerk.business.service.market.MarketAlertService;
import tech.agrowerk.infrastructure.model.market.MarketAlert;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/market-alerts")
public class MarketAlertController {

    private final MarketAlertService marketAlertService;

    public MarketAlertController(MarketAlertService marketAlertService) {
        this.marketAlertService = marketAlertService;
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<MarketAlert>> getUnread() {
        return ResponseEntity.ok(marketAlertService.getUnread());
    }

    @GetMapping("/unread/count")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<UnreadCountResponse> countUnread() {
        return ResponseEntity.ok(new UnreadCountResponse(marketAlertService.countUnread()));
    }

    @PatchMapping("/mark-as-read/{id}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        marketAlertService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> markAllAsRead() {
        marketAlertService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}