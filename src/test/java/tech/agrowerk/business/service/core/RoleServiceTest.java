package tech.agrowerk.business.service.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.agrowerk.business.service.base.BaseIntegrationTest;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.Role;
import tech.agrowerk.infrastructure.model.core.enums.RoleType;
import tech.agrowerk.infrastructure.repository.core.RoleRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleServiceTest extends BaseIntegrationTest {

    private final RoleService roleService;
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceTest(RoleService roleService, RoleRepository roleRepository) {
        this.roleService = roleService;
        this.roleRepository = roleRepository;
    }

    @BeforeAll
    static void beforeAll() {
        log.info("PostgreSQL started at: {}:{}",
                BaseIntegrationTest.postgresContainer.getHost(),
                BaseIntegrationTest.postgresContainer.getFirstMappedPort());
        log.info("Redis started at: {}:{}",
                BaseIntegrationTest.redisContainer.getHost(),
                BaseIntegrationTest.redisContainer.getMappedPort(6379));
    }

    @AfterEach
    void cleanup() {
        roleRepository.deleteAll();
        log.info("Roles table cleared after test");
    }

    @Test
    @Order(1)
    @DisplayName("1. createRole - Success: persists a new role when it does not exist")
    void testCreateRole_Success_PersistsNewRole() {
        log.info("Running Test 1: createRole persists new role");

        roleService.createRole("PRODUCER");

        assertThat(roleRepository.findByName(RoleType.PRODUCER)).isPresent();
        log.info("Role PRODUCER persisted successfully");
    }

    @Test
    @Order(2)
    @DisplayName("2. createRole - Idempotent: calling twice does not create duplicates")
    void testCreateRole_Idempotent_NoDuplicates() {
        log.info("Running Test 2: createRole idempotency");

        roleService.createRole("PRODUCER");
        roleService.createRole("PRODUCER");

        long count = roleRepository.findAll().stream()
                .filter(r -> r.getName() == RoleType.PRODUCER)
                .count();

        assertThat(count)
                .as("There should be exactly one ROLE_USER even after calling createRole twice")
                .isEqualTo(1);

        log.info("No duplicate roles created");
    }

    @Test
    @Order(3)
    @DisplayName("3. createRole - Case insensitive: lowercase input is accepted")
    void testCreateRole_CaseInsensitive_LowercaseInput() {
        log.info("Running Test 3: createRole lowercase input");

        roleService.createRole("system_admin");

        assertThat(roleRepository.findByName(RoleType.SYSTEM_ADMIN)).isPresent();
        log.info("Lowercase role name correctly converted and persisted");
    }

    @Test
    @Order(4)
    @DisplayName("4. createRole - Case insensitive: mixed case input is accepted")
    void testCreateRole_CaseInsensitive_MixedCaseInput() {
        log.info("Running Test 4: createRole mixed case input");

        roleService.createRole("Supplier_admin");

        assertThat(roleRepository.findByName(RoleType.SUPPLIER_ADMIN)).isPresent();
        log.info("Mixed case role name correctly converted and persisted");
    }

    @Test
    @Order(5)
    @DisplayName("5. createRole - Invalid role name: throws EntityNotFoundException")
    void testCreateRole_InvalidRoleName_Throws() {
        log.info("Running Test 5: createRole invalid role name");

        assertThatThrownBy(() -> roleService.createRole("ROLE_NONEXISTENT"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role type not found");

        log.info("Invalid role name correctly rejected");
    }

    @Test
    @Order(6)
    @DisplayName("6. createRole - Multiple valid roles: each is persisted independently")
    void testCreateRole_MultipleRoles_EachPersistedIndependently() {
        log.info("Running Test 6: createRole multiple roles");

        roleService.createRole("PRODUCER");
        roleService.createRole("SUPPLIER_ADMIN");

        assertThat(roleRepository.findByName(RoleType.PRODUCER)).isPresent();
        assertThat(roleRepository.findByName(RoleType.SUPPLIER_ADMIN)).isPresent();
        assertThat(roleRepository.count()).isEqualTo(2);

        log.info("Both roles persisted independently");
    }

    @Test
    @Order(7)
    @DisplayName("7. findByName - Success: returns correct role entity")
    void testFindByName_Success_ReturnsRole() {
        log.info("Running Test 7: findByName happy path");

        roleService.createRole("PRODUCER");

        Role found = roleService.findByName("PRODUCER");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(RoleType.PRODUCER);

        log.info("Role found: {}", found.getName());
    }

    @Test
    @Order(8)
    @DisplayName("8. findByName - Case insensitive: lowercase input finds the role")
    void testFindByName_CaseInsensitive_FindsRole() {
        log.info("Running Test 8: findByName lowercase input");

        roleService.createRole("PRODUCER");

        Role found = roleService.findByName("producer");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(RoleType.PRODUCER);
    }

    @Test
    @Order(9)
    @DisplayName("9. findByName - Role not in DB: throws EntityNotFoundException")
    void testFindByName_NotInDatabase_Throws() {
        log.info("Running Test 9: findByName role not in database");

        assertThatThrownBy(() -> roleService.findByName("ROLE_ADMIN"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role type not found");

        log.info("Missing role correctly throws EntityNotFoundException");
    }

    @Test
    @Order(10)
    @DisplayName("10. findByName - Invalid role name: throws EntityNotFoundException")
    void testFindByName_InvalidRoleName_Throws() {
        log.info("Running Test 10: findByName invalid role name");

        assertThatThrownBy(() -> roleService.findByName("ROLE_GHOST"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role type not found");

        log.info("Invalid role name correctly throws EntityNotFoundException");
    }

    @Test
    @Order(11)
    @DisplayName("11. findByName - Blank name: throws EntityNotFoundException via convertToRoleType")
    void testFindByName_BlankName_Throws() {
        log.info("Running Test 11: findByName blank name");

        assertThatThrownBy(() -> roleService.findByName("   "))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @Order(12)
    @DisplayName("12. Verify Testcontainers are running")
    void testContainerIsRunning() {
        log.info("Running Test 12: Container health check");

        assertThat(BaseIntegrationTest.postgresContainer.isRunning())
                .as("PostgreSQL container should be running")
                .isTrue();

        assertThat(BaseIntegrationTest.redisContainer.isRunning())
                .as("Redis container should be running")
                .isTrue();

        log.info("All containers are running properly");
    }
}
