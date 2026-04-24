package tech.agrowerk.application.controller.core;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.application.dto.response.core.RoleResponse;
import tech.agrowerk.business.service.core.RoleService;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list-roles")
    public ResponseEntity<List<RoleResponse>> listRoles() {
        return ResponseEntity.ok().body(roleService.listRoles());
    }
}
