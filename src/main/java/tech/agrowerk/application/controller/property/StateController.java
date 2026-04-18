package tech.agrowerk.application.controller.property;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.property.StateService;
import tech.agrowerk.application.dto.request.property.CreateStateRequest;
import tech.agrowerk.application.dto.request.property.UpdateStateRequest;
import tech.agrowerk.application.dto.response.property.StateResponse;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/states")
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @PostMapping("/create-state")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<StateResponse> create(@RequestBody @Valid CreateStateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stateService.createState(request));
    }

    @PutMapping("update-state/{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<StateResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateStateRequest request) {
        return ResponseEntity.ok(stateService.updateState(id, request));
    }

    @GetMapping("find-all-states")
    public ResponseEntity<List<StateResponse>> listAll() {
        return ResponseEntity.ok(stateService.listAllStates());
    }

    @GetMapping("find-state/{id}")
    public ResponseEntity<StateResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(stateService.findStateById(id));
    }

    @GetMapping("/search-state")
    public ResponseEntity<Page<StateResponse>> search(
            @RequestParam(required = false, defaultValue = "") String term,
            Pageable pageable) {
        return ResponseEntity.ok(stateService.searchStates(term, pageable));
    }
}