package tech.agrowerk.business.service.weather;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.agrowerk.application.dto.weather.location.WeatherLocationCreateRequest;
import tech.agrowerk.application.dto.weather.location.WeatherLocationDto;
import tech.agrowerk.business.service.base.BaseIntegrationTest;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherLocationServiceTest extends BaseIntegrationTest {

    private final WeatherLocationService locationService;
    private final WeatherLocationRepository repository;
    private final CacheManager redisCacheManager;

    @Autowired
    public WeatherLocationServiceTest(
            WeatherLocationService locationService,
            WeatherLocationRepository repository,
            CacheManager redisCacheManager) {
        this.locationService = locationService;
        this.repository = repository;
        this.redisCacheManager = redisCacheManager;
    }

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
        Objects.requireNonNull(redisCacheManager.getCache("weatherLocations")).clear();
    }

    @Test
    @Order(1)
    @DisplayName("1. Cache MISS and Population check")
    void testCachePopulation() {
        WeatherLocationCreateRequest request = createRequest("Horto Juazeiro");
        WeatherLocationDto saved = locationService.createLocation(request);
        UUID id = saved.id();

        locationService.findById(id);

        var cacheValue = Objects.requireNonNull(redisCacheManager.getCache("weatherLocations")).get(id);
        assertThat(cacheValue).as("Location should be cached by ID").isNotNull();
        log.info("Redis populated for ID: {}", id);
    }

    @Test
    @Order(2)
    @DisplayName("2. Global Cache Eviction (allEntries) on Create")
    void testGlobalEvictionOnCreate() {
        locationService.findAllLocations();
        assertThat(Objects.requireNonNull(redisCacheManager.getCache("weatherLocations")).get("all")).isNotNull();
        locationService.createLocation(createRequest("São Domingo - Quixeramobim"));
        assertThat(Objects.requireNonNull(redisCacheManager.getCache("weatherLocations")).get("all"))
                .as("Cache 'all' should be null after new creation")
                .isNull();
        log.info("✅ Global cache successfully evicted");
    }

    @Test
    @Order(3)
    @DisplayName("3. Cache Eviction on Delete")
    void testEvictionOnDelete() {
        WeatherLocationDto saved = locationService.createLocation(createRequest("To delete"));

        locationService.findById(saved.id());
        assertThat(Objects.requireNonNull(redisCacheManager.getCache("weatherLocations")).get(saved.id())).isNotNull();
        locationService.deleteLocation(saved.id());

        assertThat(Objects.requireNonNull(redisCacheManager.getCache("weatherLocations")).get(saved.id()))
                .as("Specific ID cache should be null after deletion")
                .isNull();
        log.info("Cache cleared after deletion");
    }

    private WeatherLocationCreateRequest createRequest(String name) {
        return new WeatherLocationCreateRequest(
                name,
                new BigDecimal("-5.1973000"),
                new BigDecimal("-39.2925000"),
                "CE",
                "BR",
                "America/Fortaleza",
                null,
                true
        );
    }
}