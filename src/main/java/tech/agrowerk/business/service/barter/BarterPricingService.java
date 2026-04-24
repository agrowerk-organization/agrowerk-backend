package tech.agrowerk.business.service.barter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.market.CommodityPriceResponse;
import tech.agrowerk.business.service.market.CommodityPriceService;
import tech.agrowerk.business.service.market.ExchangeRateService;
import tech.agrowerk.infrastructure.model.barter.BarterPriceSnapshot;
import tech.agrowerk.infrastructure.model.barter.BarterTransaction;
import tech.agrowerk.infrastructure.model.barter.BarterTransactionItem;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;
import tech.agrowerk.infrastructure.repository.barter.BarterPriceSnapshotRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BarterPricingService {

    private final CommodityPriceService commodityPriceService;
    private final ExchangeRateService exchangeRateService;
    private final BarterPriceSnapshotRepository snapshotRepository;

    public BarterPricingService(CommodityPriceService commodityPriceService,
                                ExchangeRateService exchangeRateService,
                                BarterPriceSnapshotRepository snapshotRepository) {
        this.commodityPriceService = commodityPriceService;
        this.exchangeRateService   = exchangeRateService;
        this.snapshotRepository    = snapshotRepository;
    }

    @Transactional
    public BarterPriceSnapshot captureAndPersist(BarterTransaction transaction,
                                                 List<BarterTransactionItem> items,
                                                 BigDecimal totalValues,
                                                 Commodity commodity,
                                                 BigDecimal basisUsd) {

        CommodityPriceResponse latest = commodityPriceService.getLatest(commodity);
        BigDecimal priceUsd = latest.priceUsd();

        BigDecimal ptaxRate = exchangeRateService.getUsdToBrl();
        LocalDate  ptaxDate = LocalDate.now();

        BigDecimal bagPriceBrl = priceUsd
                .add(basisUsd)
                .multiply(ptaxRate)
                .setScale(2, RoundingMode.HALF_UP);

        if (bagPriceBrl.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException(
                    "bagPriceBrl calculado inválido: " + bagPriceBrl +
                            " | priceUsd=" + priceUsd + " basis=" + basisUsd + " ptax=" + ptaxRate);

        BigDecimal totalValueBrl = items.isEmpty()
                ? (totalValues != null ? totalValues : BigDecimal.ZERO)
                : items.stream()
                .map(BarterTransactionItem::getTotalPriceBrl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalValueBrl.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Não foi possível calcular totalValueBrl - oferta sem itens estruturados " +
                            "e sem requestedValue definido"
            );
        }

        BigDecimal totalBagsDue = totalValueBrl
                .divide(bagPriceBrl, 4, RoundingMode.HALF_UP);

        log.info("Pricing snapshot captured: commodity={} priceUsd={} basis={} ptax={} " +
                        "bagBRL={} totalBRL={} bagsDue={}",
                commodity, priceUsd, basisUsd, ptaxRate, bagPriceBrl, totalValueBrl, totalBagsDue);

        BarterPriceSnapshot snapshot = BarterPriceSnapshot.builder()
                .transaction(transaction)
                .commodity(commodity.name())
                .cbotPriceUsd(priceUsd)
                .ptaxRate(ptaxRate)
                .ptaxReferenceDate(ptaxDate)
                .basisUsd(basisUsd)
                .bagPriceBrl(bagPriceBrl)
                .totalValueBrl(totalValueBrl)
                .totalBagsDue(totalBagsDue)
                .snapshotAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        return snapshotRepository.save(snapshot);
    }
}