package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.farming.CreatePrescriptionItemRequest;
import tech.agrowerk.application.dto.request.farming.CreatePrescriptionRequest;
import tech.agrowerk.application.dto.response.farming.PrescriptionResponse;
import tech.agrowerk.application.dto.response.file.FileUploadResponse;
import tech.agrowerk.business.mapper.farming.PrescriptionMapper;
import tech.agrowerk.business.service.file.FileStorageService;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.farming.AgronomicPrescription;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.PrescriptionItem;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.repository.farming.AgronomicPrescriptionRepository;
import tech.agrowerk.infrastructure.repository.farming.PlantingRepository;
import tech.agrowerk.infrastructure.repository.farming.PrescriptionItemRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AgronomicPrescriptionService {

    private final AgronomicPrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository itemRepository;
    private final PlantingRepository plantingRepository;
    private final InputRepository inputRepository;
    private final FileStorageService fileStorageService;
    private final PrescriptionMapper prescriptionMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public AgronomicPrescriptionService(AgronomicPrescriptionRepository prescriptionRepository, PrescriptionItemRepository itemRepository, PlantingRepository plantingRepository, InputRepository inputRepository, FileStorageService fileStorageService, PrescriptionMapper prescriptionMapper, OwnershipValidator ownershipValidator, AuthUtil authUtil) {
        this.prescriptionRepository = prescriptionRepository;
        this.itemRepository = itemRepository;
        this.plantingRepository = plantingRepository;
        this.inputRepository = inputRepository;
        this.fileStorageService = fileStorageService;
        this.prescriptionMapper = prescriptionMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional
    public PrescriptionResponse createPrescription(
            CreatePrescriptionRequest request,
            MultipartFile document) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(request.plantingId())
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(planting.getProperty().getId(), auth.id());

        if (planting.getPlantingStatus() != PlantingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Prescriptions can only be created for active plantings");
        }

        if (request.issuedAt().isAfter(request.validUntil())) {
            throw new IllegalArgumentException("Issue date cannot be after expiration date");
        }

        List<UUID> inputIds = request.items().stream()
                .map(CreatePrescriptionItemRequest::inputId)
                .distinct()
                .toList();

        Map<UUID, Input> inputMap = inputRepository.findAllById(inputIds).stream()
                .collect(Collectors.toMap(Input::getId, input -> input));

        if (inputMap.size() < inputIds.size()) {
            throw new EntityNotFoundException("One or more inputs were not found");
        }

        inputMap.values().forEach(input -> {
            if (!input.getControlled()) {
                throw new IllegalArgumentException("Input " + input.getName() + " is not controlled");
            }
        });

        FileUploadResponse uploadResponse = fileStorageService.upload(
                document,
                FileCategory.DOCUMENT,
                request.plantingId()
        );

        AgronomicPrescription prescription = prescriptionMapper
                .toEntity(request, planting, uploadResponse.originalUrl());

        AgronomicPrescription saved = prescriptionRepository.save(prescription);

        Set<UUID> duplicateCheck = new HashSet<>();
        List<PrescriptionItem> items = request.items().stream()
                .map(itemRequest -> {
                    Input input = inputMap.get(itemRequest.inputId());

                    if (!duplicateCheck.add(input.getId())) {
                        throw new EntityAlreadyExistsException("Input " + input.getName() + " is duplicated in request");
                    }

                    return prescriptionMapper.toItemEntity(itemRequest, saved, input);
                })
                .toList();

        itemRepository.saveAll(items);
        saved.setItems(items);

        log.info("Prescription created id={} planting={} items={}",
                saved.getId(), request.plantingId(), items.size());

        return prescriptionMapper.toResponse(saved);
    }

    @Transactional
    public PrescriptionResponse deactivatePrescription(UUID prescriptionId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        AgronomicPrescription prescription = prescriptionRepository
                .findById(prescriptionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Prescription not found"));

        ownershipValidator.validateOwnership(
                prescription.getField().getProperty().getId(), auth.id());

        prescription.setActive(false);

        log.info("Prescription deactivated id={}", prescriptionId);
        return prescriptionMapper.toResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> findByPlanting(UUID plantingId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Planting not found"));

        ownershipValidator.validateOwnership(
                planting.getProperty().getId(), auth.id());

        return prescriptionRepository.findByPlanting_Id(plantingId)
                .stream()
                .map(prescriptionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> findNearExpiration(UUID propertyId) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        LocalDate alertDate = LocalDate.now().plusDays(30);

        return prescriptionRepository
                .findNearExpirationByProperty(propertyId, alertDate)
                .stream()
                .map(prescriptionMapper::toResponse)
                .toList();
    }
}