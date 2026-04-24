/*package tech.agrowerk.application.controller.law;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import tech.agrowerk.application.dto.response.laws.LawResponse;

@Tag(name = "Laws", description = "Management and retrieval of legal documentation")
public interface LawApi {

    @Operation(
            summary = "Get law content",
            description = "Returns the content of a specific law document including metadata and HTML-rendered content."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Law content retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Law document not found"),
            @ApiResponse(responseCode = "500", description = "Error reading law document")
    })
    ResponseEntity<LawResponse> getLawContent(
            @Parameter(description = "Law slug identifier (e.g., 'lei-11326-2006')")             String fileName
    );
} */