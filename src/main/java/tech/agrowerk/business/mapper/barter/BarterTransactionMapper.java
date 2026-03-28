package tech.agrowerk.business.mapper.barter;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.barter.*;
import tech.agrowerk.infrastructure.model.barter.*;

@Component
public class BarterTransactionMapper {

    public BarterTransactionResponse toResponse(BarterTransaction t) {
        ContractSignatureStatus signatureStatus = null;

        if (t.getBarterContract() != null) {
            BarterContract c = t.getBarterContract();
            signatureStatus = new ContractSignatureStatus(
                    c.getOfferorSignedAt() != null,
                    c.getOfferorSignedAt(),
                    c.getAcceptorSignedAt() != null,
                    c.getAcceptorSignedAt()
            );
        }

        return new BarterTransactionResponse(
                t.getId(),
                t.getOffer().getId(),
                t.getOffer().getTitle(),
                t.getOfferor().getId(),
                t.getOfferor().getName(),
                t.getAcceptor().getId(),
                t.getAcceptor().getName(),
                t.getOfferorGives(),
                t.getOfferorCrop()  != null ? t.getOfferorCrop().getId()   : null,
                t.getOfferorCrop()  != null ? t.getOfferorCrop().getName() : null,
                t.getOfferorCropQuantity(),
                t.getOfferorAsset() != null ? t.getOfferorAsset().getId()   : null,
                t.getOfferorAsset() != null ? t.getOfferorAsset().getName() : null,
                t.getOfferorAssetQuantity(),
                t.getAcceptorGives(),
                t.getAcceptorCrop()  != null ? t.getAcceptorCrop().getId()   : null,
                t.getAcceptorCrop()  != null ? t.getAcceptorCrop().getName() : null,
                t.getAcceptorCropQuantity(),
                t.getStatus(),
                t.getOfferorDeliveryDate(),
                t.getAcceptorDeliveryDate(),
                t.getNotes(),
                t.getBarterContract() != null ? t.getBarterContract().getId() : null,
                signatureStatus,
                t.getCreatedAt()
        );
    }

    public BarterContractResponse toContractResponse(BarterContract c) {
        return new BarterContractResponse(
                c.getId(),
                c.getTransaction().getId(),
                c.getContractNumber(),
                c.getStartDate(),
                c.getEndDate(),
                c.getContractStatus(),
                c.getTermsAndConditions(),
                c.getOfferorSignedAt() != null,
                c.getOfferorSignedAt(),
                c.getAcceptorSignedAt() != null,
                c.getAcceptorSignedAt(),
                c.getCreatedAt()
        );
    }
}