package tech.agrowerk.business.mapper.barter;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.barter.BarterOfferResponse;
import tech.agrowerk.infrastructure.model.barter.BarterOffer;

@Component
public class BarterOfferMapper {

    public BarterOfferResponse toResponse(BarterOffer offer) {
        return new BarterOfferResponse(
                offer.getId(),
                offer.getTitle(),
                offer.getDescription(),
                offer.getOwner().getId(),
                offer.getOwner().getName(),
                offer.getProperty() != null ? offer.getProperty().getId() : null,
                offer.getProperty() != null ? offer.getProperty().getName() : null,
                offer.getOfferType(),
                offer.getOfferedCrop()  != null ? offer.getOfferedCrop().getId()   : null,
                offer.getOfferedCrop()  != null ? offer.getOfferedCrop().getName() : null,
                offer.getOfferedCropQuantity(),
                offer.getEstimatedHarvestDate(),
                offer.getOfferedAsset() != null ? offer.getOfferedAsset().getId()   : null,
                offer.getOfferedAsset() != null ? offer.getOfferedAsset().getName() : null,
                offer.getOfferedAssetQuantity(),
                offer.getRequestedType(),
                offer.getRequestedDescription(),
                offer.getRequestedValue(),
                offer.getStatus(),
                offer.getRegion(),
                offer.getExpiresAt(),
                offer.getViewCount(),
                offer.getCreatedAt()
        );
    }
}