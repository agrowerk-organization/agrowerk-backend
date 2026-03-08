package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.create.CreateAgriculturalPracticeRequest;
import tech.agrowerk.application.dto.response.AgriculturalPracticeResponse;
import tech.agrowerk.business.mapper.AgriculturalPracticeMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.IllegalArgumentException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.AgriculturalPractice;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.farming.enums.PractipeType;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.AgriculturalPracticeRepository;
import tech.agrowerk.infrastructure.repository.farming.PlantingRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AgriculturalPracticeService {

    private final AgriculturalPracticeRepository agriculturalPracticeRepository;
    private final PlantingRepository plantingRepository;
    private final UserRepository userRepository;
    private final AgriculturalPracticeMapper agriculturalPracticeMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public AgriculturalPracticeService(AgriculturalPracticeRepository agriculturalPracticeRepository, PlantingRepository plantingRepository, UserRepository userRepository, AgriculturalPracticeMapper agriculturalPracticeMapper, OwnershipValidator ownershipValidator, AuthUtil authUtil) {
        this.agriculturalPracticeRepository = agriculturalPracticeRepository;
        this.plantingRepository = plantingRepository;
        this.userRepository = userRepository;
        this.agriculturalPracticeMapper = agriculturalPracticeMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional
    public AgriculturalPracticeResponse createPractice(CreateAgriculturalPracticeRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(request.plantingId())
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id()
        );

        if (planting.getPlantingStatus() != PlantingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Agricultural practices can only be registered for IN PROGRESS plantings");
        }

        if (request.applicationDate().isBefore(planting.getPlantingDate())) {
            throw new IllegalArgumentException("Application date cannot be before planting date");
        }

        if (request.applicationDate().isAfter(planting.getExpectedHarvestDate())) {
            throw new IllegalArgumentException("Application date cannot be after expected harvest date");
        }

        User responsibleUser = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        AgriculturalPractice practice = agriculturalPracticeMapper.toEntity(
                request, planting, responsibleUser
        );

        AgriculturalPractice saved = agriculturalPracticeRepository.save(practice);

        log.info("AgriculturalPractice registered id={} planting={} type={}",
                saved.getId(), request.plantingId(), request.practipeType());

        return agriculturalPracticeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<AgriculturalPracticeResponse> findByPlanting(
            UUID plantingId, Pageable pageable) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id());

        return agriculturalPracticeRepository.findByPlanting_Id(plantingId, pageable)
                .map(agriculturalPracticeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgriculturalPracticeResponse> findByPlantingAndType(
            UUID plantingId, PractipeType type, Pageable pageable) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id());

        return agriculturalPracticeRepository
                .findByPlanting_IdAndPractipeType(plantingId, type)
                .stream()
                .map(agriculturalPracticeMapper::toResponse)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> new PageImpl<>(list, pageable, list.size())
                ));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCostByPlanting(UUID plantingId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id());

        return agriculturalPracticeRepository.sumCostByPlanting(plantingId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCostByPropertyAndPeriod(
            UUID propertyId, LocalDate start, LocalDate end) {

        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return agriculturalPracticeRepository.sumCostByPropertyAndPeriod(
                propertyId, start, end);
    }
}
