package tech.agrowerk.business.service.barter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.barter.BarterOfferItemRequest;
import tech.agrowerk.application.dto.request.barter.CreateBarterOfferRequest;
import tech.agrowerk.application.dto.request.barter.UpdateBarterOfferRequest;
import tech.agrowerk.application.dto.response.barter.BarterOfferResponse;
import tech.agrowerk.business.mapper.barter.BarterOfferMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.OperationDeniedException;
import tech.agrowerk.infrastructure.model.barter.BarterOffer;
import tech.agrowerk.infrastructure.model.barter.BarterOfferItem;
import tech.agrowerk.infrastructure.model.barter.enums.OfferStatus;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.farming.HarvestForecast;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.InventoryAsset;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.repository.barter.BarterOfferItemRepository;
import tech.agrowerk.infrastructure.repository.barter.BarterOfferRepository;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.CropRepository;
import tech.agrowerk.infrastructure.repository.farming.HarvestForecastRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;
import tech.agrowerk.infrastructure.repository.inventory.InventoryAssetRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BarterOfferService {

    private final BarterOfferRepository   offerRepository;
    private final BarterOfferItemRepository barterOfferItemRepository;
    private final UserRepository          userRepository;
    private final PropertyRepository      propertyRepository;
    private final HarvestForecastRepository harvestForecastRepository;
    private final InputRepository         inputRepository;
    private final InventoryAssetRepository assetRepository;
    private final BarterOfferMapper       barterOfferMapper;
    private final OwnershipValidator      ownershipValidator;
    private final AuthUtil                authUtil;

    public BarterOfferService(BarterOfferRepository offerRepository,
                              BarterOfferItemRepository barterOfferItemRepository,
                              UserRepository userRepository,
                              PropertyRepository propertyRepository,
                              HarvestForecastRepository harvestForecastRepository,
                              InputRepository inputRepository,
                              InventoryAssetRepository assetRepository,
                              BarterOfferMapper barterOfferMapper,
                              OwnershipValidator ownershipValidator,
                              AuthUtil authUtil) {
        this.offerRepository  = offerRepository;
        this.barterOfferItemRepository = barterOfferItemRepository;
        this.userRepository   = userRepository;
        this.propertyRepository = propertyRepository;
        this.harvestForecastRepository = harvestForecastRepository;
        this.inputRepository = inputRepository;
        this.assetRepository  = assetRepository;
        this.barterOfferMapper = barterOfferMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil         = authUtil;
    }

    @Transactional
    public BarterOfferResponse createOffer(CreateBarterOfferRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        User owner = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        ownershipValidator.validateOwnership(property.getId(), auth.id());
        validateOfferPayload(request);

        BarterOffer offer = BarterOffer.builder()
                .title(request.title())
                .description(request.description())
                .owner(owner)
                .property(property)
                .offerType(request.offerType())
                .requestedType(request.requestedType())
                .requestedDescription(request.requestedDescription())
                .requestedValue(request.requestedValue())
                .expiresAt(request.expiresAt())
                .status(OfferStatus.ACTIVE)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (request.offerType() == OfferType.CROP) {
            HarvestForecast forecast = harvestForecastRepository.findById(request.harvestForecastId())
                    .orElseThrow(() -> new EntityNotFoundException("Forecast not found"));
            offer.setOfferedForecast(forecast);
            offer.setOfferedCropQuantity(request.offeredCropQuantity());
            offer.setEstimatedHarvestDate(request.estimatedHarvestDate());
        }

        if (request.offerType() == OfferType.ASSET) {
            InventoryAsset asset = assetRepository.findById(request.offeredAssetId())
                    .orElseThrow(() -> new EntityNotFoundException("Asset not found"));
            offer.setOfferedAsset(asset);
            offer.setOfferedAssetQuantity(request.offeredAssetQuantity());
        }

        BarterOffer saved = offerRepository.save(offer);

        if (request.requestedItems() != null && !request.requestedItems().isEmpty()) {
            persistOfferItems(saved, request.requestedItems());
        }

        log.info("BarterOffer created id={} items={} by={}",
                saved.getId(),
                request.requestedItems() != null ? request.requestedItems().size() : 0,
                auth.id());

        return barterOfferMapper.toResponse(saved, resolvePropertyLocal(offer));
    }

    @Transactional(readOnly = true)
    public Page<BarterOfferResponse> listActive(Pageable pageable) {
        List<BarterOffer> content = offerRepository.findActiveWithDetails(OfferStatus.ACTIVE, pageable);
        long total = offerRepository.countByStatus(OfferStatus.ACTIVE);
        return toPageResponse(content, total, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BarterOfferResponse> listByType(OfferType offerType, Pageable pageable) {
        List<BarterOffer> content = offerRepository.findByTypeWithDetails(OfferStatus.ACTIVE, offerType, pageable);
        long total = offerRepository.countByStatusAndOfferType(OfferStatus.ACTIVE, offerType);
        return toPageResponse(content, total, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BarterOfferResponse> listMyOffers(Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        List<BarterOffer> content = offerRepository.findMyOffersWithDetails(auth.id(), pageable);
        long total = offerRepository.countByOwnerId(auth.id());
        return toPageResponse(content, total, pageable);
    }

    @Transactional
    public BarterOfferResponse findById(UUID id) {
        BarterOffer offer = offerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found"));
        offerRepository.incrementViewCount(id);
        return barterOfferMapper.toResponse(offer, resolvePropertyLocal(offer));
    }


    @Transactional
    public BarterOfferResponse updateOffer(UUID id, UpdateBarterOfferRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterOffer offer = findOfferAndValidateOwner(id, auth.id());

        if (offer.getStatus() != OfferStatus.ACTIVE)
            throw new OperationDeniedException("Only ACTIVE offers can be updated");

        if (request.title()               != null) offer.setTitle(request.title());
        if (request.description()         != null) offer.setDescription(request.description());
        if (request.requestedDescription()!= null) offer.setRequestedDescription(request.requestedDescription());
        if (request.expiresAt()           != null) offer.setExpiresAt(request.expiresAt());
        offer.setUpdatedAt(LocalDateTime.now());

        log.info("Barter offer updated id={}", id);
        return barterOfferMapper.toResponse(offer, resolvePropertyLocal(offer));
    }


    @Transactional
    public void cancelOffer(UUID id) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterOffer offer = findOfferAndValidateOwner(id, auth.id());

        if (offer.getStatus() == OfferStatus.COMPLETED)
            throw new OperationDeniedException("Completed offers cannot be cancelled");

        if (offer.getStatus() == OfferStatus.CANCELLED)
            throw new OperationDeniedException("Offer is already cancelled");

        offer.setStatus(OfferStatus.CANCELLED);
        offer.setUpdatedAt(LocalDateTime.now());
        log.info("BarterOffer cancelled id={}", id);
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expireOffers() {
        List<BarterOffer> expired = offerRepository
                .findByStatusAndExpiresAtBefore(OfferStatus.ACTIVE, LocalDate.now());

        expired.forEach(o -> {
            o.setStatus(OfferStatus.EXPIRED);
            o.setUpdatedAt(LocalDateTime.now());
        });

        if (!expired.isEmpty())
            log.info("Expired {} barter offers", expired.size());
    }

    private BarterOffer findOfferAndValidateOwner(UUID offerId, UUID userId) {
        BarterOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found"));

        if (!offer.getOwner().getId().equals(userId))
            throw new AccessDeniedException("Only the offer owner can perform this action");

        return offer;
    }

    private void persistOfferItems(BarterOffer offer, List<BarterOfferItemRequest> itemRequests) {
        List<BarterOfferItem> items = itemRequests.stream().map(req -> {
            Input input = inputRepository.findById(req.inputId())
                    .orElseThrow(() -> new EntityNotFoundException("Input not found: " + req.inputId()));

            BigDecimal total = req.unitPriceBrl()
                    .multiply(req.quantity())
                    .setScale(2, RoundingMode.HALF_UP);

            return BarterOfferItem.builder()
                    .barterOffer(offer)
                    .input(input)
                    .quantity(req.quantity())
                    .unitOfMeasure(req.unitOfMeasure())
                    .unitPriceBrl(req.unitPriceBrl())
                    .totalPriceBrl(total)
                    .notes(req.notes())
                    .createdAt(LocalDateTime.now())
                    .build();
        }).toList();

        barterOfferItemRepository.saveAll(items);
        log.info("BarterOfferItems saved: {} items for offer={}", items.size(), offer.getId());
    }


    private Page<BarterOfferResponse> toPageResponse(List<BarterOffer> content, long total, Pageable pageable) {
        if (!content.isEmpty()) {
            List<UUID> ids = content.stream().map(BarterOffer::getId).toList();
            offerRepository.fetchRequestedItems(ids);
        }
        Page<BarterOffer> page = new PageImpl<>(content, pageable, total);
        return page.map(o -> barterOfferMapper.toResponse(o, resolvePropertyLocal(o)));
    }

    private void validateOfferPayload(CreateBarterOfferRequest request) {
        if (request.offerType() == OfferType.CROP) {
            if (request.harvestForecastId() == null || request.offeredCropQuantity() == null)
                throw new IllegalArgumentException("Crop offer requires offeredCropId and offeredCropQuantity");
        }
        if (request.offerType() == OfferType.ASSET) {
            if (request.offeredAssetId() == null || request.offeredAssetQuantity() == null)
                throw new IllegalArgumentException("Asset offer requires offeredAssetId and offeredAssetQuantity");
        }
    }

    private String resolvePropertyLocal(BarterOffer offer) {
        if (offer.getProperty() == null) return null;
        var property = offer.getProperty();
        var address = property.getAddress();
        if (address == null) return null;
        var state = property.getState();
        return address.getMunicipality() + " / " + (state != null ? state.getName() : "");
    }
}
