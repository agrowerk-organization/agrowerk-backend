package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.inventory.CreateInputCropRequest;
import tech.agrowerk.application.dto.response.inventory.InputCropResponse;
import tech.agrowerk.business.mapper.inventory.InputCropMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.InputCrop;
import tech.agrowerk.infrastructure.model.supplier.Supplier;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.CropRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputCropRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;
import tech.agrowerk.infrastructure.repository.supplier.SupplierRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class InputCropService {

    private final InputCropRepository inputCropRepository;
    private final InputRepository inputRepository;
    private final CropRepository cropRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final InputCropMapper inputCropMapper;
    private final AuthUtil authUtil;

    public InputCropService(InputCropRepository inputCropRepository,
                            InputRepository inputRepository,
                            CropRepository cropRepository,
                            SupplierRepository supplierRepository,
                            UserRepository userRepository,
                            InputCropMapper inputCropMapper,
                            AuthUtil authUtil) {
        this.inputCropRepository = inputCropRepository;
        this.inputRepository = inputRepository;
        this.cropRepository = cropRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.inputCropMapper = inputCropMapper;
        this.authUtil = authUtil;
    }

    @Transactional
    public InputCropResponse suggestInputCrop(
            CreateInputCropRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Input input = inputRepository.findById(request.inputId())
                .orElseThrow(() -> new EntityNotFoundException("Input not found"));

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found"));

        if (user.isSupplierAdmin()) {
            if (input.getSupplier() == null ||
                    !input.getSupplier().getAdministrator()
                            .getId().equals(auth.id())) {
                throw new AccessDeniedException("You can only suggest crops for your own inputs"
                );
            }
        }

        if (!input.getActive()) {
            throw new IllegalArgumentException(
                    "Cannot suggest crop for inactive input"
            );
        }

        Crop crop = cropRepository.findById(request.cropId())
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));

        if (inputCropRepository.existsByInput_IdAndCrop_Id(
                request.inputId(), request.cropId())) {
            throw new EntityAlreadyExistsException("Suggestion already exists for this input and crop"
            );
        }

        InputCrop inputCrop = inputCropMapper.toEntity(
                request, input, crop);
        InputCrop saved = inputCropRepository.save(inputCrop);

        log.info("InputCrop suggested id={} input={} crop={}",
                saved.getId(), request.inputId(), request.cropId());

        return inputCropMapper.toResponse(saved);
    }

    @Transactional
    public InputCropResponse approve(UUID inputCropId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        InputCrop inputCrop = inputCropRepository.findById(inputCropId)
                .orElseThrow(() -> new EntityNotFoundException("InputCrop not found"));

        if (inputCrop.getApprovedByAdmin()) {
            throw new IllegalArgumentException("Already approved");
        }

        User admin = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found"));

        inputCrop.setApprovedByAdmin(true);
        inputCrop.setApprovedBy(admin);
        inputCrop.setApprovedAt(Instant.now());

        log.info("InputCrop approved id={} by admin={}",
                inputCropId, auth.id());

        return inputCropMapper.toResponse(inputCrop);
    }

    @Transactional
    public void reject(UUID inputCropId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        InputCrop inputCrop = inputCropRepository.findById(inputCropId)
                .orElseThrow(() -> new EntityNotFoundException("Input crop not found"));

        if (inputCrop.getApprovedByAdmin()) {
            throw new IllegalArgumentException("Cannot reject an already approved suggestion");
        }

        inputCropRepository.delete(inputCrop);
        log.info("InputCrop rejected id={} by admin={}",
                inputCropId, auth.id());
    }

    @Transactional(readOnly = true)
    public List<InputCropResponse> findApprovedByCrop(UUID cropId) {
        return inputCropRepository
                .findByCrop_IdAndApprovedByAdminTrue(cropId)
                .stream()
                .map(inputCropMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InputCropResponse> findPending() {
        return inputCropRepository.findByApprovedByAdminFalse()
                .stream()
                .map(inputCropMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InputCropResponse> findMyPending() {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Supplier supplier = supplierRepository
                .findByAdministrator_Id(auth.id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found"));

        return inputCropRepository
                .findPendingBySupplierId(supplier.getId())
                .stream()
                .map(inputCropMapper::toResponse)
                .toList();
    }
}
