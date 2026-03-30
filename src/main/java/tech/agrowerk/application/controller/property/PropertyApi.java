package tech.agrowerk.application.controller.property;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.property.AddOwnerRequest;
import tech.agrowerk.application.dto.request.property.CreatePropertyRequest;
import tech.agrowerk.application.dto.request.property.UpdatePropertyRequest;
import tech.agrowerk.application.dto.response.file.FileUploadResponse;
import tech.agrowerk.application.dto.response.property.PropertyResponse;
import tech.agrowerk.infrastructure.model.property.enums.OwnerRemovalReason;

import java.util.UUID;

@Tag(name = "Properties", description = "Management of agricultural properties, ownership, and documentation")
public interface PropertyApi {

    @Operation(summary = "Register new property", description = "Creates a new agricultural property for the producer.")
    @ApiResponse(responseCode = "201", description = "Property created successfully")
    ResponseEntity<PropertyResponse> create(@Valid @RequestBody CreatePropertyRequest request);

    @Operation(summary = "Get property details", description = "Retrieves full details of a property by its unique ID.")
    ResponseEntity<PropertyResponse> findPropertyById(@PathVariable UUID propertyId);

    @Operation(summary = "List my properties", description = "Returns a paginated list of properties where the current user is an owner.")
    ResponseEntity<Page<PropertyResponse>> myProperties(@Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Update property information", description = "Updates general information of a specific property.")
    ResponseEntity<PropertyResponse> update(@PathVariable UUID propertyId, @Valid @RequestBody UpdatePropertyRequest request);

    @Operation(summary = "Add co-owner", description = "Adds another user as a co-owner of the property.")
    ResponseEntity<Void> addOwner(@PathVariable UUID propertyId, @Valid @RequestBody AddOwnerRequest request);

    @Operation(summary = "Remove owner", description = "Removes a user's ownership from the property with a specified reason.")
    ResponseEntity<Void> removeOwner(
            @PathVariable UUID propertyId,
            @PathVariable UUID targetUserId,
            @RequestParam OwnerRemovalReason reason);

    @Operation(summary = "Update edit permissions", description = "Grants or revokes editing rights for a specific co-owner.")
    ResponseEntity<Void> updateEditPermission(
            @PathVariable UUID propertyId,
            @PathVariable UUID targetUserId,
            @RequestParam boolean canEdit);

    @Operation(summary = "Upload property photo", description = "Uploads an image for the property. Replaces existing photo.")
    @ApiResponse(responseCode = "201", description = "Photo uploaded successfully")
    ResponseEntity<FileUploadResponse> uploadPhoto(
            @PathVariable UUID propertyId,
            @Parameter(description = "Multipart image file") MultipartFile file);
}