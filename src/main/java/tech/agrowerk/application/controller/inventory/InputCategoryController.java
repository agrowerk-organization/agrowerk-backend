package tech.agrowerk.application.controller.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.inventory.CreateInputCategoryRequest;
import tech.agrowerk.application.dto.request.inventory.UpdateInputCategoryRequest;
import tech.agrowerk.application.dto.response.inventory.InputCategoryResponse;
import tech.agrowerk.business.service.inventory.InputCategoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/input-categories")
public class InputCategoryController {
    private final InputCategoryService inputCategoryService;

    public InputCategoryController(InputCategoryService inputCategoryService) {
        this.inputCategoryService = inputCategoryService;
    }

    @PostMapping("/create-input-category")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<InputCategoryResponse> createInputCategory(@Valid @RequestBody CreateInputCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inputCategoryService.createCategory(request));
    }

    @PutMapping("/{categoryId}/update")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<InputCategoryResponse> update(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateInputCategoryRequest request) {
        return ResponseEntity.ok(
                inputCategoryService.updateCategory(categoryId, request));
    }

    @PatchMapping("deactivate/{categoryId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deactivateInputCategory(
            @PathVariable UUID categoryId) {
        inputCategoryService.deactivateCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/find-tree")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InputCategoryResponse>> findTree() {
        return ResponseEntity.ok(inputCategoryService.findTree());
    }

    @GetMapping("/find-flat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InputCategoryResponse>> findFlat() {
        return ResponseEntity.ok(inputCategoryService.findFlat());
    }

    @GetMapping("/find-by-id/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InputCategoryResponse> findById(
            @PathVariable UUID categoryId) {
        return ResponseEntity.ok(inputCategoryService.findById(categoryId));
    }
}
