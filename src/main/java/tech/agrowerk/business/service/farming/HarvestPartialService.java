package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.farming.CreateHarvestPartialRequest;
import tech.agrowerk.application.dto.request.farming.UpdateHarvestPartialRequest;
import tech.agrowerk.application.dto.response.farming.HarvestPartialResponse;
import tech.agrowerk.business.listener.events.HarvestPartialAddedEvent;
import tech.agrowerk.business.listener.events.HarvestPartialUpdatedEvent;
import tech.agrowerk.business.mapper.farming.HarvestPartialMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Harvest;
import tech.agrowerk.infrastructure.model.farming.HarvestPartial;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.HarvestPartialRepository;
import tech.agrowerk.infrastructure.repository.farming.HarvestRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class HarvestPartialService {

    private final HarvestPartialRepository harvestPartialRepository;
    private final HarvestRepository harvestRepository;
    private final UserRepository userRepository;
    private final HarvestPartialMapper harvestPartialMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;
    private final ApplicationEventPublisher eventPublisher;

    public HarvestPartialService(HarvestPartialRepository harvestPartialRepository, HarvestRepository harvestRepository, UserRepository userRepository, HarvestPartialMapper harvestPartialMapper, OwnershipValidator ownershipValidator, AuthUtil authUtil, ApplicationEventPublisher eventPublisher) {
        this.harvestPartialRepository = harvestPartialRepository;
        this.harvestRepository = harvestRepository;
        this.userRepository = userRepository;
        this.harvestPartialMapper = harvestPartialMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public HarvestPartialResponse addPartial(UUID harvestId,
                                             CreateHarvestPartialRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Harvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new EntityNotFoundException("Harvest not found"));

        ownershipValidator.validateOwnership(
                harvest.getPlanting().getProperty().getId(), auth.id());

        if (harvest.getFinalized()) {
            throw new IllegalArgumentException(
                    "Cannot add partials to a finalized harvest"
            );
        }

        if (request.partialDate().isBefore(
                harvest.getPlanting().getPlantingDate())) {
            throw new IllegalArgumentException(
                    "Partial date cannot be before planting date"
            );
        }

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        HarvestPartial partial = harvestPartialMapper.toPartialEntity(
                request, harvest, user);
        harvestPartialRepository.save(partial);

        eventPublisher.publishEvent(new HarvestPartialAddedEvent(
                partial.getId(),
                harvest.getId(),
                harvest.getPlanting().getId(),
                harvest.getPlanting().getProperty().getId(),
                partial.getQuantityKg(),
                harvest.getPlanting().getCropVariety().getCrop().getName(),
                auth.id()
        ));

        log.info("HarvestPartial added id={} harvest={} quantityKg={}",
                partial.getId(), harvestId, request.quantityKg());

        BigDecimal totalQuantity = harvestPartialRepository
                .sumQuantityByHarvest(harvestId);

        return harvestPartialMapper.toPartialResponse(partial, totalQuantity);
    }

    @Transactional
    public HarvestPartialResponse updatePartial(UUID partialId,
                                                UpdateHarvestPartialRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        HarvestPartial partial = harvestPartialRepository.findById(partialId)
                .orElseThrow(() -> new EntityNotFoundException("Partial not found"));

        ownershipValidator.validateOwnership(
                partial.getHarvest().getPlanting().getProperty().getId(), auth.id());

        if (partial.getHarvest().getFinalized()) {
            throw new IllegalArgumentException(
                    "Cannot edit partial of a finalized harvest"
            );
        }

        boolean hasChanges = false;
        BigDecimal previousQuantity = partial.getQuantityKg();

        if (request.quantityKg() != null) {
            partial.setQuantityKg(request.quantityKg());
            hasChanges = true;
        }
        if (request.qualityGrade() != null) {
            partial.setQualityGrade(request.qualityGrade());
            hasChanges = true;
        }
        if (request.notes() != null) {
            partial.setNotes(request.notes());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for partial id={}", partialId);
            return harvestPartialMapper.toPartialResponse(partial, BigDecimal.ZERO);
        }

        if (request.quantityKg() != null &&
                request.quantityKg().compareTo(previousQuantity) != 0) {
            eventPublisher.publishEvent(new HarvestPartialUpdatedEvent(
                    partial.getId(),
                    partial.getHarvest().getId(),
                    partial.getHarvest().getPlanting().getProperty().getId(),
                    previousQuantity,
                    request.quantityKg(),
                    partial.getHarvest().getPlanting()
                            .getCropVariety().getCrop().getName()
            ));
        }

        log.info("HarvestPartial updated id={}", partialId);
        return harvestPartialMapper.toPartialResponse(partial, request.quantityKg());
    }

    @Transactional(readOnly = true)
    public Page<HarvestPartialResponse> findByHarvest(UUID harvestId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Harvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new EntityNotFoundException("Harvest not found"));

        ownershipValidator.validateOwnership(
                harvest.getPlanting().getProperty().getId(), auth.id());

        BigDecimal totalAccumulated = harvestPartialRepository.sumQuantityByHarvest(harvestId);

        return harvestPartialRepository.findByHarvest_Id(harvestId, pageable)
                .map(partial -> harvestPartialMapper.toPartialResponse(partial, totalAccumulated));
    }
}