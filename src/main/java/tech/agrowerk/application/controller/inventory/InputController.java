package tech.agrowerk.application.controller.inventory;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.inventory.CreateInputRequest;
import tech.agrowerk.application.dto.request.inventory.UpdateInputRequest;
import tech.agrowerk.application.dto.response.inventory.InputResponse;
import tech.agrowerk.business.service.inventory.InputService;

import java.util.UUID;

@RestController
@RequestMapping("/inputs")
public class InputController {

    private final InputService inputService;

    public InputController(InputService inputService) {
        this.inputService = inputService;
    }

    @PostMapping("/create-input")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') " +
            "or hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<InputResponse> createInput(
            @Valid @RequestBody CreateInputRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inputService.createInput(request));
    }

    @PatchMapping("/update-input/{inputId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') " +
            "or hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<InputResponse> updateInput(
            @PathVariable UUID inputId,
            @Valid @RequestBody UpdateInputRequest request) {
        return ResponseEntity.ok(
                inputService.updateInput(inputId, request));
    }

    @PatchMapping("/deactivate-input/{inputId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') " +
            "or hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<Void> deactivateInput(
            @PathVariable UUID inputId) {
        inputService.deactivateInput(inputId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/find-catalog")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<InputResponse>> findCatalog(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                inputService.findAllForProducer(pageable));
    }

    @GetMapping("/find-by-category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<InputResponse>> findByCategory(
            @PathVariable UUID categoryId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                inputService.findByCategory(categoryId, pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<InputResponse>> search(
            @RequestParam String name, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                inputService.searchByName(name, pageable));
    }

    @GetMapping("/my-inputs")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<Page<InputResponse>> myInputs(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                inputService.findMyInputs(pageable));
    }

    @GetMapping("/find-by-id/{inputId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InputResponse> findById(
            @PathVariable UUID inputId) {
        return ResponseEntity.ok(
                inputService.findById(inputId));
    }
}