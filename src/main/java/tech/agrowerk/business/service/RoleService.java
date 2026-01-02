package tech.agrowerk.business.service;

import lombok.val;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.enums.RoleType;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.Role;
import tech.agrowerk.infrastructure.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void createRole(String role) {

        val roleType = convertToRoleType(role);

        roleRepository.findByName(roleType)
                .orElseGet(() -> {
                    var newRole = new Role();
                    newRole.setName(roleType);
                    return roleRepository.save(newRole);
                });
    }

    public Role findByName(String name) {
        RoleType roleType = convertToRoleType(name);

        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new EntityNotFoundException("Role not found."));
    }

    private RoleType convertToRoleType(String name) {
        try {
            return RoleType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Role type not found.");
        }
    }
}
