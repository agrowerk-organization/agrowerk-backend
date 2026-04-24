package tech.agrowerk.infrastructure.repository.barter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.BarterOfferItem;

import java.util.List;
import java.util.UUID;

@Repository
public interface BarterOfferItemRepository extends JpaRepository<BarterOfferItem, UUID> {
    List<BarterOfferItem> findByBarterOffer_Id(UUID barterOfferId);
}
