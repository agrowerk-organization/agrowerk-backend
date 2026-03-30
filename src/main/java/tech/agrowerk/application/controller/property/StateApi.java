package tech.agrowerk.application.controller.property;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tech.agrowerk.application.dto.request.property.CreateStateRequest;
import tech.agrowerk.application.dto.request.property.UpdateStateRequest;
import tech.agrowerk.application.dto.response.property.StateResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = "States", description = "Management of Brazilian states and administrative regions")
public interface StateApi {

    @Operation(summary = "Create a new state", description = "Adds a new state to the system. Restricted to SYSTEM_ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "State created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    ResponseEntity<StateResponse> create(@Valid @RequestBody CreateStateRequest request);

    @Operation(summary = "Update state", description = "Updates details of an existing state by ID. Restricted to SYSTEM_ADMIN.")
    ResponseEntity<StateResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateStateRequest request);

    @Operation(summary = "List all states", description = "Returns a simple list of all registered states.")
    ResponseEntity<List<StateResponse>> listAll();

    @Operation(summary = "Find state by ID", description = "Retrieves details of a specific state.")
    ResponseEntity<StateResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Search states", description = "Returns a paginated list of states filtered by a search term.")
    ResponseEntity<Page<StateResponse>> search(
            @Parameter(description = "Search term (name or initials)") @RequestParam(required = false) String term,
            @Parameter(hidden = true) Pageable pageable
    );
}