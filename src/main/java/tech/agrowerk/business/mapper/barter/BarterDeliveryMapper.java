package tech.agrowerk.business.mapper.barter;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.barter.CropCommitmentResponse;
import tech.agrowerk.application.dto.response.barter.PartialDeliveryResponse;
import tech.agrowerk.infrastructure.model.barter.CropCommitment;
import tech.agrowerk.infrastructure.model.barter.PartialDelivery;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BarterDeliveryMapper {

    public CropCommitmentResponse toCommitmentResponse(CropCommitment c) {
        BigDecimal pending = c.getCommittedQuantity().subtract(c.getDeliveredQuantity());

        BigDecimal progress = c.getCommittedQuantity().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : c.getDeliveredQuantity()
                .multiply(BigDecimal.valueOf(100))
                .divide(c.getCommittedQuantity(), 2, RoundingMode.HALF_UP);

        return new CropCommitmentResponse(
                c.getId(),
                c.getTransaction().getId(),
                c.getFarmer().getId(),
                c.getFarmer().getName(),
                c.getCrop().getId(),
                c.getCrop().getName(),
                c.getCommittedQuantity(),
                c.getDeliveredQuantity(),
                pending,
                progress,
                c.getExpectedDeliveryDate(),
                c.getActualDeliveryDate(),
                c.getStatus(),
                c.getNotes()
        );
    }

    public PartialDeliveryResponse toDeliveryResponse(PartialDelivery p) {
        return new PartialDeliveryResponse(
                p.getId(),
                p.getCommitment().getId(),
                p.getDeliveredQuantity(),
                p.getDeliveryDate(),
                p.getMoisturePercentage(),
                p.getImpurityPercentage(),
                p.getQualityGrade(),
                p.getNotes()
        );
    }
}