package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.inventory.CreateInputCategoryRequest;
import tech.agrowerk.application.dto.request.inventory.UpdateInputCategoryRequest;
import tech.agrowerk.application.dto.response.inventory.InputCategoryResponse;
import tech.agrowerk.business.mapper.inventory.InputCategoryMapper;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.IllegalArgumentException;
import tech.agrowerk.infrastructure.model.inventory.InputCategory;
import tech.agrowerk.infrastructure.repository.inventory.InputCategoryRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class InputCategoryService {

    private final InputCategoryRepository inputCategoryRepository;
    private final InputCategoryMapper inputCategoryMapper;

    public InputCategoryService(InputCategoryRepository inputCategoryRepository, InputCategoryMapper inputCategoryMapper) {
        this.inputCategoryRepository = inputCategoryRepository;
        this.inputCategoryMapper = inputCategoryMapper;
    }

    @CacheEvict(value = "inputCategories", allEntries = true,
                cacheManager = "redisCacheManager")
    @Transactional
    public InputCategoryResponse createCategory(
            CreateInputCategoryRequest request) {

        if (inputCategoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new EntityAlreadyExistsException(
                    "Category already exists: " + request.name());
        }

        InputCategory parent = null;

        if (request.parentId() != null) {
            parent = inputCategoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent category not found"));

            if (!parent.getIsActive()) {
                throw new IllegalArgumentException(
                        "Cannot create child of inactive category"
                );
            }

            if (parent.getLevel() >= 2) {
                throw new IllegalArgumentException(
                        "Maximum category depth is 3 levels"
                );
            }
        }

        InputCategory category = inputCategoryMapper.toEntity(request, parent);
        InputCategory saved = inputCategoryRepository.save(category);

        log.info("InputCategory created id={} level={}",
                saved.getId(), saved.getLevel());

        return inputCategoryMapper.toResponse(saved);
    }

    @CacheEvict(value = "inputCategories", allEntries = true,
            cacheManager = "redisCacheManager")
    @Transactional
    public InputCategoryResponse updateCategory(UUID categoryId,
                                                UpdateInputCategoryRequest request) {

        InputCategory category = inputCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found"));

        if (request.name() != null && !request.name().isBlank()) {
            if (inputCategoryRepository.existsByNameIgnoreCaseAndIdNot(
                    request.name(), categoryId)) {
                throw new EntityAlreadyExistsException(
                        "Category name already exists"
                );
            }
            category.setName(request.name());
        }
        if (request.description() != null) {
            category.setDescription(request.description());
        }
        if (request.unitOfMeasure() != null) {
            category.setUnitOfMeasure(request.unitOfMeasure());
        }
        if (request.icon() != null) {
            category.setIcon(request.icon());
        }
        if (request.color() != null) {
            category.setColor(request.color());
        }
        if (request.hazardLevel() != null) {
            category.setHazardLevel(request.hazardLevel());
        }

        log.info("InputCategory updated id={}", categoryId);
        return inputCategoryMapper.toResponse(category);
    }

    @CacheEvict(value = "inputCategories", allEntries = true,
            cacheManager = "redisCacheManager")
    @Transactional
    public void deactivateCategory(UUID categoryId) {

        InputCategory category = inputCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found"));

        if (inputCategoryRepository.existsByParent_IdAndIsActiveTrue(categoryId)) {
            throw new IllegalArgumentException(
                    "Cannot deactivate category with active children"
            );
        }

        if (inputCategoryRepository.existsByIdAndInputs_ActiveTrue(categoryId)) {
            throw new IllegalArgumentException(
                    "Cannot deactivate category with active inputs"
            );
        }

        category.setIsActive(false);
        log.info("InputCategory deactivated id={}", categoryId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "inputCategories", key = "'tree'",
            cacheManager = "redisCacheManager")
    public List<InputCategoryResponse> findTree() {
        return inputCategoryRepository.findByParentIsNullAndIsActiveTrue()
                .stream()
                .map(inputCategoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "inputCategories", key = "'flat'",
            cacheManager = "redisCacheManager")
    public List<InputCategoryResponse> findFlat() {
        return inputCategoryRepository.findAll().stream()
                .filter(InputCategory::getIsActive)
                .map(inputCategoryMapper::toFlatResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InputCategoryResponse findById(UUID categoryId) {
        return inputCategoryRepository.findById(categoryId)
                .map(inputCategoryMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found"));
    }
}