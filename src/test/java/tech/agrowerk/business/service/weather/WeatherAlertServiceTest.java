package tech.agrowerk.business.service.weather;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tech.agrowerk.infrastructure.model.weather.WeatherAlert;
import tech.agrowerk.infrastructure.model.weather.WeatherCurrent;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.model.weather.enums.WeatherAlertSeverity;
import tech.agrowerk.infrastructure.model.weather.enums.WeatherAlertType;
import tech.agrowerk.infrastructure.repository.weather.WeatherAlertRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherAlertServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    private final WeatherAlertService alertService;
    private final WeatherAlertRepository alertRepository;
    private final WeatherLocationRepository locationRepository;
    private final CacheManager cacheManager;

    private static WeatherLocation quixeramobim;

    @Autowired
    public WeatherAlertServiceTest(
            WeatherAlertService alertService,
            WeatherAlertRepository alertRepository,
            WeatherLocationRepository locationRepository,
            CacheManager cacheManager) {
        this.alertService = alertService;
        this.alertRepository = alertRepository;
        this.locationRepository = locationRepository;
        this.cacheManager = cacheManager;
    }

    @BeforeEach
    void setup() {
        alertRepository.deleteAll();
        locationRepository.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("weatherAlerts")).clear();

        quixeramobim = WeatherLocation.builder()
                .name("Igreja Matriz - Quixeramobim")
                .latitude(new BigDecimal("-5.1973000"))
                .longitude(new BigDecimal("-39.2925000"))
                .state("CE")
                .country("BR")
                .timezone("America/Fortaleza")
                .active(true)
                .build();
        quixeramobim = locationRepository.save(quixeramobim);
    }

    @Test
    @Order(1)
    @DisplayName("Should generate CRITICAL Frost Alert when temperature is low")
    void testFrostAlertGeneration() {
        WeatherCurrent coldWeather = WeatherCurrent.builder()
                .location(quixeramobim)
                .temperature(BigDecimal.valueOf(1.0))
                .humidity(40)
                .timestamp(Instant.now())
                .build();

        List<WeatherAlert> alerts = alertService.processWeatherDataForAlerts(coldWeather);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getAlertType()).isEqualTo(WeatherAlertType.FROST);
        assertThat(alerts.getFirst().getSeverity()).isEqualTo(WeatherAlertSeverity.CRITICAL);
        assertThat(alertRepository.count()).isEqualTo(1);

        log.info("Frost alert generated for Quixeramobim");
    }

    @Test
    @Order(2)
    @DisplayName("Should NOT generate duplicate alerts within the 6-hour window")
    void testDuplicateAlertDeduplication() {
        WeatherCurrent hotWeather = WeatherCurrent.builder()
                .location(quixeramobim)
                .temperature(BigDecimal.valueOf(38.0))
                .timestamp(Instant.now())
                .build();

        alertService.processWeatherDataForAlerts(hotWeather);
        List<WeatherAlert> secondBatch = alertService.processWeatherDataForAlerts(hotWeather);

        assertThat(secondBatch).isEmpty();
        assertThat(alertRepository.countByLocation(quixeramobim)).isEqualTo(1);

        log.info("Deduplication logic working correctly");
    }

    @Test
    @Order(3)
    @DisplayName("Should generate Disease Risk Alert for high humidity")
    void testDiseaseRiskAlert() {
        WeatherCurrent humidWeather = WeatherCurrent.builder()
                .location(quixeramobim)
                .temperature(BigDecimal.valueOf(22.0))
                .humidity(92)
                .timestamp(Instant.now())
                .build();

        List<WeatherAlert> alerts = alertService.processWeatherDataForAlerts(humidWeather);

        assertThat(alerts).extracting(WeatherAlert::getAlertType)
                .contains(WeatherAlertType.DISEASE_FAVORABLE);
        log.info("Fungal disease risk detected correctly");
    }

    @Test
    @Order(4)
    @DisplayName("Should clear cache when resolving an alert")
    void testCacheEvictionOnResolution() {
        WeatherCurrent current = WeatherCurrent.builder()
                .location(quixeramobim)
                .temperature(BigDecimal.valueOf(40.0))
                .timestamp(Instant.now())
                .build();

        List<WeatherAlert> created = alertService.processWeatherDataForAlerts(current);
        UUID alertId = created.getFirst().getId();

        alertService.getActiveAlertsByLocation(quixeramobim);
        assertThat(Objects.requireNonNull(cacheManager.getCache("weatherAlerts")).get(quixeramobim.getId())).isNotNull();

        alertService.resolveAlert(alertId, "Admin_Test");

        assertThat(Objects.requireNonNull(cacheManager.getCache("weatherAlerts")).get(quixeramobim.getId())).isNull();
        log.info("Alert resolution evicted cache successfully");
    }

    @Test
    @Order(5)
    @DisplayName("Should generate HIGH Heat Wave Alert when temperature is high")
    void testHeatWaveAlert() {
        WeatherCurrent hot = WeatherCurrent.builder()
                .location(quixeramobim)
                .temperature(BigDecimal.valueOf(41.0))
                .timestamp(Instant.now())
                .build();

        List<WeatherAlert> alerts = alertService.processWeatherDataForAlerts(hot);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getAlertType()).isEqualTo(WeatherAlertType.HEAT_WAVE);
        assertThat(alerts.getFirst().getSeverity()).isEqualTo(WeatherAlertSeverity.HIGH);
        assertThat(alerts.getFirst().getTitle()).contains("Alerta de onda de calor");
    }

    @Test
    @Order(6)
    @DisplayName("Should NOT generate alert when conditions are normal")
    void testNoAlertNormalConditions() {
        WeatherCurrent normal = WeatherCurrent.builder()
                .location(quixeramobim)
                .temperature(BigDecimal.valueOf(25.0))
                .humidity(60)
                .rainfall(BigDecimal.ZERO)
                .windSpeed(BigDecimal.valueOf(5.0))
                .timestamp(Instant.now())
                .build();

        List<WeatherAlert> alerts = alertService.processWeatherDataForAlerts(normal);

        assertThat(alerts).isEmpty();
        assertThat(alertRepository.count()).isZero();
    }

    @Test
    @Order(7)
    @DisplayName("Should generate CRITICAL Heavy Rain Alert above 80mm")
    void testHeavyRainCritical() {
        WeatherCurrent flood = WeatherCurrent.builder()
                .location(quixeramobim)
                .rainfall(BigDecimal.valueOf(90.0))
                .timestamp(Instant.now())
                .build();

        List<WeatherAlert> alerts = alertService.processWeatherDataForAlerts(flood);

        assertThat(alerts.getFirst().getSeverity()).isEqualTo(WeatherAlertSeverity.CRITICAL);
    }
}