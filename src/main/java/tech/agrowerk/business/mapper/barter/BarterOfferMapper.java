package tech.agrowerk.business.mapper.barter;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.barter.BarterOfferItemResponse;
import tech.agrowerk.application.dto.response.barter.BarterOfferResponse;
import tech.agrowerk.infrastructure.model.barter.BarterOffer;
import tech.agrowerk.infrastructure.model.barter.BarterOfferItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class BarterOfferMapper {

    public BarterOfferResponse toResponse(BarterOffer offer,
                                          String propertyLocal,
                                          BigDecimal suggestedQuantity,
                                          BigDecimal referencePrice,
                                          LocalDate referencePriceDate
    ) {
        return new BarterOfferResponse(
                offer.getId(),
                offer.getTitle(),
                offer.getDescription(),
                offer.getOwner().getId(),
                offer.getOwner().getName(),
                offer.getProperty() != null ? offer.getProperty().getId() : null,
                offer.getProperty() != null ? offer.getProperty().getName() : null,
                propertyLocal,
                offer.getOfferType(),
                offer.getOfferedForecast() != null ? offer.getOfferedForecast().getId() : null,
                offer.getOfferedForecast() != null ? offer.getOfferedForecast().getCrop().getName() : null,
                offer.getOfferedCropQuantity(),
                offer.getEstimatedHarvestDate(),
                offer.getOfferedAsset() != null ? offer.getOfferedAsset().getId()   : null,
                offer.getOfferedAsset() != null ? offer.getOfferedAsset().getName() : null,
                offer.getOfferedAssetQuantity(),
                offer.getRequestedType(),
                offer.getRequestedDescription(),
                offer.getRequestedValue(),
                offer.getStatus(),
                offer.getExpiresAt(),
                offer.getViewCount(),
                mapItems(offer.getRequestedItems()),
                suggestedQuantity,
                referencePrice,
                referencePriceDate,
                offer.getCreatedAt()
        );
    }

    private List<BarterOfferItemResponse> mapItems(List<BarterOfferItem> items) {
        if (items == null || items.isEmpty()) return List.of();
        return items.stream()
                .map(item -> new BarterOfferItemResponse(
                        item.getId(),
                        item.getInput().getId(),
                        item.getInput().getName(),
                        item.getQuantity(),
                        item.getUnitOfMeasure().toString(),
                        item.getUnitPriceBrl(),
                        item.getTotalPriceBrl(),
                        item.getInput().getAveragePurchasePrice(),
                        item.getNotes(),
                        item.getInput().getUpdatedAt()
                ))
                .toList();
    }
}