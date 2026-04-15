package tech.agrowerk.application.controller.market;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.business.service.market.MarketReportService;
import tech.agrowerk.infrastructure.model.market.MarketReport;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;

@RestController
@RequestMapping("/market-reports")
public class MarketReportController {

    private final MarketReportService marketReportService;

    public MarketReportController(MarketReportService marketReportService) {
        this.marketReportService = marketReportService;
    }

    @PostMapping("/generate/{reportType}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<MarketReport> generate(@PathVariable ReportType reportType) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(marketReportService.generate(reportType));

    }

    @GetMapping("/latest/{reportType}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<MarketReport> getLatestWeekly(@PathVariable ReportType reportType) {
        return marketReportService.getLatestByType(reportType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
