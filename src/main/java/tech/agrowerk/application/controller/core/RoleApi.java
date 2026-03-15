package tech.agrowerk.application.controller.core;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import tech.agrowerk.application.dto.response.core.RoleResponse;

import java.util.List;

@Tag(name = "Roles", description = "Operations for role profile")
public interface RoleApi {
    @Operation(summary = "List all roles", description = "Returns a list of roles")
    ResponseEntity<List<RoleResponse>> listRoles();
}