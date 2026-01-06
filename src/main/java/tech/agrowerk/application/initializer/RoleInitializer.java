package tech.agrowerk.application.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tech.agrowerk.business.service.RoleService;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.repository.RoleRepository;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final RoleService roleService;

    public RoleInitializer(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) {
        List<String> roles = Arrays.asList("SYSTEM_ADMIN", "SUPPLIER_ADMIN", "PRODUCER");

        for (String roleName : roles) {
            Object role;

            try {
                role = roleService.findByName(roleName);
            }
            catch (EntityNotFoundException e) {
                role = null;
            }

            if (role == null) {
                roleService.createRole(roleName);
                System.out.println("Role created: " + roleName);
            }
            else {
                System.out.println("Role already exists: " + roleName);
            }
        }
    }
}