package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.market.CommodityDashboardResponse;
import tech.agrowerk.application.dto.market.CommodityHistoryResponse;
import tech.agrowerk.application.dto.market.CommodityPriceResponse;
import tech.agrowerk.application.dto.market.MarketPrice;
import tech.agrowerk.business.mapper.market.CommodityPriceMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.infrastructure.exception.local.MarketDataException;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;
import tech.agrowerk.infrastructure.repository.market.CommodityPriceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommodityPriceService {

    private static final int DEFAULT_HISTORY_DAYS = 30;

    private final CommodityPriceRepository commodityPriceRepository;
    private final CommodityPriceMapper commodityPriceMapper;

    public CommodityPriceService(CommodityPriceRepository commodityPriceRepository,
                                 CommodityPriceMapper commodityPriceMapper) {
        this.commodityPriceRepository = commodityPriceRepository;
        this.commodityPriceMapper = commodityPriceMapper;
    }

   @Transactional(readOnly = true)
   public CommodityDashboardResponse getDashboard() {
        List<CommodityPrice> latestEntities = commodityPriceRepository.findLatestPricePerCommodity();

        List<CommodityPriceResponse> latestResponse = latestEntities.stream()
                .map(p -> commodityPriceMapper.toResponse(p, getPrevious(p).orElse(null)))
                .toList();

        LocalDate since = LocalDate.now().minusDays(365);
        List<Commodity> commodities = List.of(Commodity.values());

        Map<Commodity, List<CommodityPriceResponse>> historyMap =
                commodityPriceRepository.findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
                        commodities, since, LocalDate.now()
                ).stream()
                        .collect(Collectors.groupingBy(
                                CommodityPrice::getCommodity,
                                Collectors.mapping(commodityPriceMapper::toResponse, Collectors.toList())
                        ));

        return new CommodityDashboardResponse(latestResponse, historyMap);
   }

    @Transactional(readOnly = true)
    public CommodityPriceResponse getLatest(Commodity commodity) {
        CommodityPrice current = commodityPriceRepository
                .findTopByCommodityOrderByReferenceDateDesc(commodity)
                .orElseThrow(() -> new MarketDataException("No data for " + commodity));

        return commodityPriceMapper.toResponse(current, getPrevious(current).orElse(null));
    }

    @Transactional(readOnly = true)
    public CommodityHistoryResponse getHistory(Commodity commodity, int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        List<CommodityPrice> prices = commodityPriceRepository
                .findByCommodityAndReferenceDateBetweenOrderByReferenceDateDesc(
                        commodity, since, LocalDate.now());

        List<CommodityPriceResponse> responses = prices.stream()
                .map(commodityPriceMapper::toResponse)
                .toList();

        return new CommodityHistoryResponse(commodity, responses, responses.size());
    }

    @Transactional
    public void saveFromMarketRecord(MarketPrice record) {
        if (commodityPriceRepository.existsByCommodityAndSourceAndReferenceDate(
                record.commodity(), record.source(), record.referenceDate())) {
            log.debug("Already exists: {} {} {}", record.commodity(), record.source(), record.referenceDate());
            return;
        }

        CommodityPrice price = CommodityPrice.builder()
                .commodity(record.commodity())
                .price(record.priceBrl())
                .priceUsd(record.priceUsd())
                .exchangeRate(record.exchangeRate())
                .unit(record.unit())
                .source(record.source())
                .referenceDate(record.referenceDate())
                .fetchedAt(LocalDateTime.now())
                .build();

        commodityPriceRepository.save(price);
        log.debug("Saved {} {} {}", record.commodity(), record.priceBrl(), record.unit());
    }

    private Optional<CommodityPrice> getPrevious(CommodityPrice current) {
        return commodityPriceRepository.findFirstByCommodityAndReferenceDateBeforeOrderByReferenceDateDesc(
                        current.getCommodity(),  current.getReferenceDate());
    }
}
