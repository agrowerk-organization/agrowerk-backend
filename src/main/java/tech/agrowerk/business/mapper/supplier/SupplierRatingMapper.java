package tech.agrowerk.business.mapper.supplier;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.supplier.SupplierRatingResponse;
import tech.agrowerk.infrastructure.model.supplier.SupplierRating;

@Component
public class SupplierRatingMapper {

    public SupplierRatingResponse toRatingResponse(SupplierRating rating) {
        return new SupplierRatingResponse(
                rating.getId(),
                rating.getSupplier().getId(),
                rating.getRatedBy().getName(),
                rating.getRating(),
                rating.getComment(),
                rating.getCreatedAt()
        );
    }

}
