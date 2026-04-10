package tech.agrowerk.application.controller.market;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.market.ExchangeRateService;

import java.time.LocalDate;

@RestController
@RequestMapping("/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping("/admin/backfill-exchange-rates")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> backfillExchangeRates() {
        exchangeRateService.backfillHistoricalRates(LocalDate.of(2025, 1, 1), LocalDate.now());
        return ResponseEntity.accepted().build();
    }
}
