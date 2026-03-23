package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.market.CommodityDashboardResponse;
import tech.agrowerk.application.dto.market.CommodityHistoryResponse;
import tech.agrowerk.application.dto.market.CommodityPriceResponse;
import tech.agrowerk.business.mapper.market.CommodityPriceMapper;
import tech.agrowerk.infrastructure.exception.local.MarketDataException;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;
import tech.agrowerk.infrastructure.repository.market.CommodityPriceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CommodityPriceService {

    private static final int DEFAULT_HISTORY_DAYS = 30;

    private final CommodityPriceRepository commodityPriceRepository;
    private final CommodityPriceMapper commodityPriceMapper;

    public CommodityPriceService(CommodityPriceRepository commodityPriceRepository, CommodityPriceMapper commodityPriceMapper) {
        this.commodityPriceRepository = commodityPriceRepository;
        this.commodityPriceMapper = commodityPriceMapper;
    }

    @Transactional(readOnly = true)
    public CommodityDashboardResponse getDashboard() {
        List<CommodityPriceResponse> latest = commodityPriceRepository.findLatestPricePerCommodity()
                .stream()
                .map(p -> commodityPriceMapper.toResponse(p, getPrevious(p).orElse(null)))
                .toList();

        return new CommodityDashboardResponse(
                latest,
                getHistory(Commodity.SOJA, DEFAULT_HISTORY_DAYS).prices(),
                getHistory(Commodity.MILHO, DEFAULT_HISTORY_DAYS).prices(),
                getHistory(Commodity.FEIJAO, DEFAULT_HISTORY_DAYS).prices()
        );
    }

    @Transactional(readOnly = true)
    public CommodityPriceResponse getLatest(Commodity commodity) {
        CommodityPrice current = commodityPriceRepository
                .findTopByCommodityOrderByReferenceDateDesc(
                        commodity
                ).orElseThrow(() -> new MarketDataException("No data for " + commodity));

        return commodityPriceMapper.toResponse(current, getPrevious(current).orElse(null));
    }

    @Transactional(readOnly = true)
    public CommodityHistoryResponse getHistory(Commodity commodity, int days) {
        LocalDate since = LocalDate.now().minusDays(days);

        List<CommodityPrice> prices = commodityPriceRepository.findByCommodityAndReferenceDateBetweenOrderByReferenceDateDesc(
                commodity, since, LocalDate.now()
        );

        List<CommodityPriceResponse> responses = prices.stream()
                .map(commodityPriceMapper::toResponse)
                .toList();

        return new CommodityHistoryResponse(commodity, responses, responses.size());
    }

    @Transactional
    public void saveIfNotExists(CommodityPrice commodityPrice) {
        if (!commodityPriceRepository.existsByCommodityAndRegionAndReferenceDate(
                commodityPrice.getCommodity(), commodityPrice.getRegion(), commodityPrice.getReferenceDate()
        )) {
            commodityPriceRepository.save(commodityPrice);

            log.debug("Saved {} - {} - {}", commodityPrice.getCommodity(),
                    commodityPrice.getRegion(), commodityPrice.getReferenceDate());
        }
    }

    private Optional<CommodityPrice> getPrevious(CommodityPrice current) {
        LocalDate yesterday = current.getReferenceDate().minusDays(1);

        return commodityPriceRepository.findByCommodityAndReferenceDateBetweenOrderByReferenceDateDesc(
                        current.getCommodity(), yesterday.minusDays(5), yesterday
                ).stream()
                .findFirst();
    }
}
