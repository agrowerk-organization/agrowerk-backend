package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.farming.CreateHarvestForecastRequest;
import tech.agrowerk.application.dto.request.farming.UpdateHarvestForecastRequest;
import tech.agrowerk.application.dto.response.farming.HarvestForecastResponse;
import tech.agrowerk.application.dto.projection.HarvestQuantityProjection;
import tech.agrowerk.business.mapper.farming.HarvestForecastMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.IllegalArgumentException;
import tech.agrowerk.infrastructure.model.farming.HarvestForecast;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.farming.enums.SeasonStatus;
import tech.agrowerk.infrastructure.repository.farming.HarvestForecastRepository;
import tech.agrowerk.infrastructure.repository.farming.HarvestPartialRepository;
import tech.agrowerk.infrastructure.repository.farming.HarvestRepository;
import tech.agrowerk.infrastructure.repository.farming.PlantingRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HarvestForecastService {

    private final HarvestForecastRepository harvestForecastRepository;
    private final PlantingRepository plantingRepository;
    private final HarvestRepository harvestRepository;
    private final HarvestPartialRepository harvestPartialRepository;
    private final HarvestForecastMapper harvestForecastMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public HarvestForecastService(HarvestForecastRepository harvestForecastRepository,
                                  PlantingRepository plantingRepository,
                                  HarvestRepository harvestRepository, HarvestPartialRepository harvestPartialRepository,
                                  HarvestForecastMapper harvestForecastMapper,
                                  OwnershipValidator ownershipValidator,
                                  AuthUtil authUtil) {
        this.harvestForecastRepository = harvestForecastRepository;
        this.plantingRepository = plantingRepository;
        this.harvestRepository = harvestRepository;
        this.harvestPartialRepository = harvestPartialRepository;
        this.harvestForecastMapper = harvestForecastMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional
    public HarvestForecastResponse createForecast(CreateHarvestForecastRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(request.plantingId())
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(planting.getProperty().getId(), auth.id());

        if (planting.getSeason().getSeasonStatus() != SeasonStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Forecasts can only be created for IN PROGRESS seasons");
        }

        if (planting.getPlantingStatus() != PlantingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Forecasts can only be created for IN PROGRESS plantings");
        }

        if (request.forecastDate().isBefore(planting.getPlantingDate())) {
            throw new IllegalArgumentException("Forecast date cannot be before planting date");
        }

        HarvestForecast harvestForecast = harvestForecastMapper.toEntity(request, planting);
        HarvestForecast saved = harvestForecastRepository.save(harvestForecast);

        log.info("HarvestForecast created id={} planting={} confidence={}",
                saved.getId(), request.plantingId(), request.confidenceLevel());

        return harvestForecastMapper.toResponse(saved, null);
    }

    @Transactional
    public HarvestForecastResponse updateForecast(UUID forecastId,
                                                  UpdateHarvestForecastRequest request) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        HarvestForecast forecast = harvestForecastRepository.findById(forecastId)
                .orElseThrow(() -> new EntityNotFoundException("Forecast not found"));

        ownershipValidator.validateOwnership(
                forecast.getPlanting().getProperty().getId(), auth.id());

        if (forecast.getPlanting().getPlantingStatus() == PlantingStatus.HARVESTED) {
            throw new IllegalArgumentException(
                    "Cannot update forecast after harvest"
            );
        }

        boolean hasChanges = false;

        if (request.estimatedQuantity() != null) {
            forecast.setEstimatedQuantity(request.estimatedQuantity());
            hasChanges = true;
        }
        if (request.forecastDate() != null) {
            forecast.setForecastDate(request.forecastDate());
            hasChanges = true;
        }
        if (request.confidenceLevel() != null) {
            forecast.setConfidenceLevel(request.confidenceLevel());
            hasChanges = true;
        }
        if (request.plantedArea() != null) {
            forecast.setPlantedArea(request.plantedArea());
            hasChanges = true;
        }
        if (request.notes() != null) {
            forecast.setNotes(request.notes());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for forecast id={}", forecastId);
        }

        log.info("HarvestForecast updated id={}", forecastId);

        BigDecimal actualQuantity = harvestRepository.sumTotalQuantityByPlantingId(forecast.getPlanting().getId());

        return harvestForecastMapper.toResponse(forecast, actualQuantity);
    }

    @Transactional(readOnly = true)
    public Page<HarvestForecastResponse> findByPlanting(UUID plantingId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id());

        BigDecimal actualQuantity = harvestRepository.sumTotalQuantityByPlantingId(plantingId);

        return harvestForecastRepository.findByPlanting_Id(plantingId, pageable)
                .map(f -> harvestForecastMapper.toResponse(f, actualQuantity));
    }

    @Transactional(readOnly = true)
    public HarvestForecastResponse findByPlantingAndForecastDate(UUID plantingId, LocalDate forecastdate) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id());

        BigDecimal actualQuantity = harvestRepository.sumTotalQuantityByPlantingId(plantingId);

        HarvestForecast response = harvestForecastRepository.findByPlanting_IdAndForecastDate(plantingId, forecastdate)
                .orElseThrow(() -> new EntityNotFoundException("Forecast harvest not found"));

        return harvestForecastMapper.toResponse(response, actualQuantity);
    }

    @Transactional(readOnly = true)
    public Page<HarvestForecastResponse> findLatestByCropAndSeason(
            UUID propertyId, UUID seasonId, UUID cropId, Pageable pageable) {

        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        Map<UUID, BigDecimal> quantityMap = harvestPartialRepository
                .findQuantitiesByPropertyAndSeason(propertyId, seasonId, pageable)
                .stream()
                .collect(Collectors.toMap(
                        HarvestQuantityProjection::harvestId,
                        HarvestQuantityProjection::totalQuantityKg,
                        (existing, replacement) -> existing
                ));

        return harvestForecastRepository
                .findLatestByCropAndSeason(seasonId, propertyId, cropId, pageable)
                .map(f -> {
                    BigDecimal actual = null;
                    if (f.getPlanting() != null && f.getPlanting().getHarvest() != null) {
                        actual = quantityMap.get(f.getPlanting().getHarvest().getId());
                    }

                    return harvestForecastMapper.toResponse(f, actual);
                });
    }

    @Transactional(readOnly = true)
    public Page<HarvestForecastResponse> findByPropertyAndSeason(
            UUID propertyId, UUID seasonId, Pageable pageable) {

        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        Map<UUID, BigDecimal> quantityMap = harvestPartialRepository
                .findQuantitiesByPropertyAndSeason(propertyId, seasonId, pageable)
                .stream()
                .collect(Collectors.toMap(
                        HarvestQuantityProjection::harvestId,
                        HarvestQuantityProjection::totalQuantityKg
                ));

        return harvestForecastRepository
                .findByPlanting_Property_IdAndPlanting_Season_Id(
                        propertyId, seasonId, pageable)
                .map(f -> {
                    BigDecimal actual = f.getPlanting().getHarvest() != null
                            ? quantityMap.getOrDefault(
                            f.getPlanting().getHarvest().getId(),
                            null)
                            : null;
                    return harvestForecastMapper.toResponse(f, actual);
                });
    }
}
