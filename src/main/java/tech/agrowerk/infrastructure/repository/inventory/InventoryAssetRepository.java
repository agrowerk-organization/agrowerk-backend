package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.InventoryAsset;
import tech.agrowerk.infrastructure.model.inventory.enums.AssetCategory;

import java.util.UUID;

@Repository
public interface InventoryAssetRepository extends JpaRepository<InventoryAsset, UUID> {

    Page<InventoryAsset> findByOwner_IdAndAvailableTrue(UUID ownerId, Pageable pageable);

    Page<InventoryAsset> findByProperty_Id(UUID propertyId, Pageable pageable);

    Page<InventoryAsset> findByApprovedForBarterTrueAndAvailableTrue(Pageable pageable);

    Page<InventoryAsset> findByApprovedForBarterFalseAndAvailableTrue(Pageable pageable);

    Page<InventoryAsset> findByCategory(AssetCategory category, Pageable pageable);

}
