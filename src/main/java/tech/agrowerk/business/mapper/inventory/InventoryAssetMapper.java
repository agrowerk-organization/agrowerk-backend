package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.inventory.CreateInventoryAssetRequest;
import tech.agrowerk.application.dto.response.inventory.InventoryAssetResponse;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.inventory.InventoryAsset;
import tech.agrowerk.infrastructure.model.property.Property;

import java.util.List;

@Component
public class InventoryAssetMapper {

    public InventoryAsset toEntity(CreateInventoryAssetRequest request,
                                   User owner,
                                   Property property) {
        InventoryAsset asset = new InventoryAsset();
        asset.setName(request.name());
        asset.setDescription(request.description());
        asset.setCategory(request.category());
        asset.setCondition(request.condition());
        asset.setQuantity(request.quantity());
        asset.setReferenceValue(request.referenceValue());
        asset.setUnit(request.unit());
        asset.setOwner(owner);
        asset.setProperty(property);
        asset.setAvailable(true);
        asset.setApprovedForBarter(false);
        asset.setValuationMethod(request.valuationMethod());
        asset.setAgreedValue(request.agreedValue());
        asset.setCommodityReference(request.commodityReference());
        asset.setCommodityQuantityEquivalent(
                request.commodityQuantityEquivalent());
        return asset;
    }

    public InventoryAssetResponse toResponse(InventoryAsset asset,
                                             List<String> photoUrls) {
        return new InventoryAssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getDescription(),
                asset.getCategory().name(),
                asset.getCondition().name(),
                asset.getQuantity(),
                asset.getReferenceValue(),
                asset.getUnit(),
                asset.getAvailable(),
                asset.getApprovedForBarter(),
                asset.getApprovedBy() != null
                        ? asset.getApprovedBy().getId() : null,
                asset.getApprovedBy() != null
                        ? asset.getApprovedBy().getName() : null,
                asset.getApprovedAt(),
                asset.getApprovalNotes(),
                asset.getValuationMethod().name(),
                asset.getAgreedValue(),
                asset.getCommodityReference(),
                asset.getCommodityQuantityEquivalent(),
                asset.getOwner().getId(),
                asset.getOwner().getName(),
                asset.getProperty() != null
                        ? asset.getProperty().getId() : null,
                asset.getProperty() != null
                        ? asset.getProperty().getName() : null,
                photoUrls,
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }
}
