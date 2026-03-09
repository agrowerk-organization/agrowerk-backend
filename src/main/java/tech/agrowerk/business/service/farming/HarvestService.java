package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.create.CreateHarvestRequest;
import tech.agrowerk.application.dto.response.HarvestResponse;
import tech.agrowerk.business.listener.events.HarvestFinalizedEvent;
import tech.agrowerk.business.mapper.HarvestMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Harvest;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.Yield;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.*;
import tech.agrowerk.infrastructure.repository.inventory.StockMovementRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class HarvestService {

    private final HarvestRepository harvestRepository;
    private final HarvestPartialRepository harvestPartialRepository;
    private final PlantingRepository plantingRepository;
    private final StockRepository stockRepository;
    private final AgriculturalPracticeRepository practiceRepository;
    private final HarvestMapper harvestMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;
    private final ApplicationEventPublisher eventPublisher;

    public HarvestService(HarvestRepository harvestRepository,
                          HarvestPartialRepository harvestPartialRepository,
                          PlantingRepository plantingRepository,
                          StockRepository stockRepository,
                          AgriculturalPracticeRepository practiceRepository,
                          HarvestMapper harvestMapper,
                          OwnershipValidator ownershipValidator,
                          AuthUtil authUtil,
                          ApplicationEventPublisher eventPublisher) {
        this.harvestRepository = harvestRepository;
        this.harvestPartialRepository = harvestPartialRepository;
        this.plantingRepository = plantingRepository;
        this.stockRepository = stockRepository;
        this.practiceRepository = practiceRepository;
        this.harvestMapper = harvestMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public HarvestResponse createHarvest(CreateHarvestRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(request.plantingId())
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id());

        if (planting.getPlantingStatus() != PlantingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException(
                    "Only IN_PROGRESS plantings can be harvested"
            );
        }

        if (harvestRepository.existsByPlanting_Id(request.plantingId())) {
            throw new EntityAlreadyExistsException(
                    "Harvest already exists for this planting"
            );
        }

        Harvest harvest = harvestMapper.toEntity(request, planting, null);
        Harvest saved = harvestRepository.save(harvest);

        log.info("Harvest created id={} planting={} — pending partials",
                saved.getId(), request.plantingId());

        return harvestMapper.toResponse(saved,
                calculateTotalPlantingCost(planting),
                BigDecimal.ZERO);
    }

    @Transactional
    public HarvestResponse finalizeHarvest(UUID harvestId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Harvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new EntityNotFoundException("Harvest not found"));

        ownershipValidator.validateOwnership(
                harvest.getPlanting().getProperty().getId(), auth.id());

        if (harvest.getFinalized()) {
            throw new IllegalArgumentException("Harvest already finalized");
        }

        BigDecimal totalQuantity = harvestPartialRepository
                .sumQuantityByHarvest(harvestId);

        if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "Cannot finalize harvest without any partial registered"
            );
        }

        harvest.setFinalized(true);
        harvest.setFinalizedAt(LocalDate.now());

        BigDecimal totalCost = calculateTotalPlantingCost(harvest.getPlanting());

        eventPublisher.publishEvent(new HarvestFinalizedEvent(
                harvest.getId(),
                harvest.getPlanting().getId(),
                harvest.getPlanting().getProperty().getId(),
                harvest.getPlanting().getField().getId(),
                harvest.getPlanting().getAreaHectares(),
                totalQuantity,
                totalCost,
                harvest.getPlanting().getCropVariety().getCrop().getName(),
                auth.id()
        ));

        log.info("Harvest finalized id={} totalQuantityKg={} event published",
                harvestId, totalQuantity);

        return harvestMapper.toResponse(harvest, totalCost, totalQuantity);
    }

    @Transactional(readOnly = true)
    public HarvestResponse findByPlanting(UUID plantingId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id());

        Harvest harvest = harvestRepository.findByPlanting_Id(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Harvest not found"));

        BigDecimal totalQuantity = harvestPartialRepository
                .sumQuantityByHarvest(harvest.getId());

        return harvestMapper.toResponse(harvest,
                calculateTotalPlantingCost(planting), totalQuantity);
    }

    @Transactional(readOnly = true)
    public Page<HarvestResponse> findByProperty(UUID propertyId, Pageable pageable) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return harvestRepository.findByPlanting_Property_Id(propertyId, pageable)
                .map(h -> {
                    BigDecimal total = harvestPartialRepository
                            .sumQuantityByHarvest(h.getId());
                    BigDecimal cost = calculateTotalPlantingCost(h.getPlanting());
                    return harvestMapper.toResponse(h, cost, total);
                });
    }

    private BigDecimal calculateTotalPlantingCost(Planting planting) {
        BigDecimal inputCost = planting.getPlantingInputs().stream()
                .map(pi -> pi.getQuantity()
                        .multiply(pi.getInput().getAveragePurchasePrice() != null
                                ? pi.getInput().getAveragePurchasePrice()
                                : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal practiceCost = practiceRepository
                .sumCostByPlanting(planting.getId());

        return inputCost.add(practiceCost);
    }
}