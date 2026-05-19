package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.inventory.CreateInputRequest;
import tech.agrowerk.application.dto.request.inventory.UpdateInputRequest;
import tech.agrowerk.application.dto.response.inventory.InputResponse;
import tech.agrowerk.business.mapper.inventory.InputMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.IllegalArgumentException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.InputCategory;
import tech.agrowerk.infrastructure.model.supplier.Supplier;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputCategoryRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;
import tech.agrowerk.infrastructure.repository.supplier.SupplierRepository;

import javax.swing.*;
import java.util.UUID;

@Service
@Slf4j
public class InputService {
    private final InputRepository inputRepository;
    private final InputCategoryRepository inputCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final InputMapper inputMapper;
    private final AuthUtil authUtil;

    public InputService(InputRepository inputRepository, InputCategoryRepository inputCategoryRepository, SupplierRepository supplierRepository, UserRepository userRepository, InputMapper inputMapper, AuthUtil authUtil) {
        this.inputRepository = inputRepository;
        this.inputCategoryRepository = inputCategoryRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.inputMapper = inputMapper;
        this.authUtil = authUtil;
    }

    @Transactional
    public InputResponse createInput(CreateInputRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        InputCategory inputCategory = inputCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category input not found"));

        if (!inputCategory.getIsActive()) {
            throw new IllegalArgumentException("Cannot create input for inactive category");
        }

        if (request.internalCode() != null && inputRepository.existsByInternalCode(request.internalCode())) {
            throw new IllegalArgumentException("Internal code already exists");
        }

        Supplier supplier = null;
        boolean globalVisible = true;

        if (user.isSupplierAdmin()) {
            supplier = supplierRepository.findByAdministrator_Id(auth.id())
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found for this admin"));
            globalVisible = false;
        }

        Input input = inputMapper.toEntity(request, inputCategory, supplier, globalVisible);
        Input saved = inputRepository.save(input);

        log.info("Input created id={} globalVisible={} supplier={}", saved.getId(), globalVisible,
                supplier != null ? supplier.getId() : "none");

        return inputMapper.toResponse(saved);
    }

    @Transactional
    public InputResponse updateInput(UUID inputId,
                                     UpdateInputRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Input input = inputRepository.findById(inputId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Input not found"));

        validateEditPermission(input, auth);

        boolean hasChanges = false;

        if (request.name() != null && !request.name().isBlank()) {
            input.setName(request.name());
            hasChanges = true;
        }
        if (request.description() != null) {
            input.setDescription(request.description());
            hasChanges = true;
        }
        if (request.unitOfMeasure() != null) {
            input.setUnitOfMeasure(request.unitOfMeasure());
            hasChanges = true;
        }
        if (request.activeIngredient() != null) {
            input.setActiveIngredient(request.activeIngredient());
            hasChanges = true;
        }
        if (request.formulation() != null) {
            input.setFormulation(request.formulation());
            hasChanges = true;
        }
        if (request.concentration() != null) {
            input.setConcentration(request.concentration());
            hasChanges = true;
        }
        if (request.mapaRegistration() != null) {
            input.setMapaRegistration(request.mapaRegistration());
            hasChanges = true;
        }
        if (request.toxicologicalClass() != null) {
            input.setToxicologicalClass(request.toxicologicalClass());
            hasChanges = true;
        }
        if (request.gracePeriod() != null) {
            input.setGracePeriod(request.gracePeriod());
            hasChanges = true;
        }
        if (request.minimumStock() != null) {
            input.setMinimumStock(request.minimumStock());
            hasChanges = true;
        }
        if (request.maximumStock() != null) {
            input.setMaximumStock(request.maximumStock());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for input id={}", inputId);
        }

        log.info("Input updated id={}", inputId);
        return inputMapper.toResponse(input);
    }

    @Transactional
    public void deactivateInput(UUID inputId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Input input = inputRepository.findById(inputId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Input not found"));

        validateEditPermission(input, auth);

        if (inputRepository.hasActiveStock(inputId)) {
            throw new IllegalArgumentException(
                    "Cannot deactivate input with active stock — " +
                            "consume or adjust stock first"
            );
        }

        input.setActive(false);
        log.info("Input deactivated id={}", inputId);
    }

    @Transactional(readOnly = true)
    public Page<InputResponse> findAllForProducer(Pageable pageable) {
        return inputRepository.findAllVisibleToProducer(pageable)
                .map(inputMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InputResponse> findByCategory(
            UUID categoryId, Pageable pageable) {
        return inputRepository
                .findByCategory_IdAndActiveTrue(categoryId, pageable)
                .map(inputMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InputResponse> searchByName(
            String name, Pageable pageable) {
        return inputRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(inputMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InputResponse> findMyInputs(Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Supplier supplier = supplierRepository
                .findByAdministrator_Id(auth.id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found"));

        return inputRepository
                .findBySupplier_IdAndActiveTrue(supplier.getId(), pageable)
                .map(inputMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InputResponse findById(UUID inputId) {
        return inputRepository.findById(inputId)
                .map(inputMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Input not found"));
    }

    private void validateEditPermission(Input input, AuthenticatedUser auth) {
        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getIsSystemAdmin()) && input.getSupplier() != null) {
            throw new AccessDeniedException("System admin can only edit global inputs");
        }

        if (user.isSupplierAdmin()) {
            Supplier supplier = supplierRepository.findByAdministrator_Id(auth.id())
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

            if (input.getSupplier() == null || !input.getSupplier().getId().equals(supplier.getId())) {
                throw new AccessDeniedException("You can only edit your own supplier inputs");
            }
        }
    }
}
