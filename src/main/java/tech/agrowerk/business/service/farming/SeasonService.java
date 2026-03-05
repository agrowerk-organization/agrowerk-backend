package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.create.CreateSeasonRequest;
import tech.agrowerk.application.dto.response.SeasonResponse;
import tech.agrowerk.business.mapper.SeasonMapper;
import tech.agrowerk.business.service.property.PropertyService;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.Season;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.farming.enums.SeasonStatus;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.repository.farming.PlantingRepository;
import tech.agrowerk.infrastructure.repository.farming.SeasonRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final PropertyRepository propertyRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final PlantingRepository plantingRepository;
    private final SeasonMapper seasonMapper;
    private final AuthUtil authUtil;
    private final OwnershipValidator ownershipValidator;

    public SeasonService(SeasonRepository seasonRepository,
                         PropertyRepository propertyRepository,
                         UserPropertyRepository userPropertyRepository,
                         PlantingRepository plantingRepository,
                         SeasonMapper seasonMapper,
                         AuthUtil authUtil, OwnershipValidator ownershipValidator) {
        this.seasonRepository = seasonRepository;
        this.propertyRepository = propertyRepository;
        this.userPropertyRepository = userPropertyRepository;
        this.plantingRepository = plantingRepository;
        this.seasonMapper = seasonMapper;
        this.authUtil = authUtil;
        this.ownershipValidator = ownershipValidator;
    }

    @Transactional
    public SeasonResponse createSeason(CreateSeasonRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        ownershipValidator.validateOwnership(request.propertyId(), auth.id());

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (seasonRepository.existsByPropertyIdAndName(request.propertyId(), request.name())) {
            throw new EntityAlreadyExistsException("Season name already exists for this property");
        }

        if (seasonRepository.existsOverlappingSeasons(request.propertyId(), request.startDate(), request.endDate())) {
            throw new IllegalArgumentException("Season dates overlap with an existing season");
        }

        Season season = seasonMapper.toEntity(request, property);
        Season saved = seasonRepository.save(season);

        log.info("Season created id={} property={}", saved.getId(), request.propertyId());
        return seasonMapper.toResponse(saved);
    }

    @Transactional
    public SeasonResponse activateSeason(UUID seasonId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Season season = findAndValidateOwnership(seasonId, auth.id());

        seasonRepository.findByPropertyIdAndSeasonStatus(
                season.getProperty().getId(), SeasonStatus.IN_PROGRESS
        ).ifPresent(s -> {
            throw new IllegalArgumentException("There is already an active season for this property");
        });

        if (season.getSeasonStatus() != SeasonStatus.PLANNED) {
            throw new IllegalArgumentException("Only PLANNED seasons can be activated");
        }

        season.setSeasonStatus(SeasonStatus.IN_PROGRESS);
        log.info("Season activated id={}", seasonId);
        return seasonMapper.toResponse(season);
    }

    @Transactional
    public SeasonResponse finishSeason(UUID seasonId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Season season = findAndValidateOwnership(seasonId, auth.id());

        if (season.getSeasonStatus() != SeasonStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Only ACTIVE seasons can be finished");
        }

        List<Planting> activePlantings = plantingRepository
                .findBySeasonIdAndPlantingStatus(seasonId, PlantingStatus.FINISHED);

        activePlantings.forEach(p -> {
            p.setPlantingStatus(PlantingStatus.FINISHED);
            log.warn("Planting id={} finished by season closure", p.getId());
        });

        season.setSeasonStatus(SeasonStatus.FINISHED);
        log.info("Season finished id={}", seasonId);
        return seasonMapper.toResponse(season);
    }

    @Cacheable(value = "seasons", key = "#propertyId",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public Page<SeasonResponse> findMySeasons(UUID propertyId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateOwnership(propertyId, auth.id());

        return seasonRepository.findByPropertyId(propertyId, pageable)
                .map(seasonMapper::toResponse);
    }

    private Season findAndValidateOwnership(UUID seasonId, UUID userId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new EntityNotFoundException("Season not found"));

        ownershipValidator.validateOwnership(season.getProperty().getId(), userId);
        return season;
    }
}
