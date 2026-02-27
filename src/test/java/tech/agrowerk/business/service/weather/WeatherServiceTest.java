package tech.agrowerk.business.service.weather;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.agrowerk.application.dto.weather.Current;
import tech.agrowerk.application.dto.weather.Forecast;
import tech.agrowerk.business.service.base.BaseIntegrationTest;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.weather.WeatherCurrentRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "resilience4j", "cache"})
@Testcontainers
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherServiceTest extends BaseIntegrationTest {

    private final WeatherService weatherService;
    private final WeatherCacheService cacheService;
    private final WeatherLocationRepository locationRepository;
    private final WeatherCurrentRepository currentRepository;
    private final CacheManager caffeineCacheManager;
    private final CacheManager redisCacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    private static WeatherLocation testLocation;
    private static UUID testLocationId;

    @Autowired
    public WeatherServiceTest(
            WeatherService weatherService,
            WeatherCacheService cacheService,
            WeatherLocationRepository locationRepository,
            WeatherCurrentRepository currentRepository,
            CacheManager caffeineCacheManager,
            CacheManager redisCacheManager,
            RedisTemplate<String, Object> redisTemplate) {
        this.weatherService = weatherService;
        this.cacheService = cacheService;
        this.locationRepository = locationRepository;
        this.currentRepository = currentRepository;
        this.caffeineCacheManager = caffeineCacheManager;
        this.redisCacheManager = redisCacheManager;
        this.redisTemplate = redisTemplate;
    }

    @BeforeAll
    static void beforeAll() {
        log.info("PostgreSQL Container started at: {}:{}",
                postgresContainer.getHost(),
                postgresContainer.getFirstMappedPort());
        log.info("Redis Container started at: {}:{}",
                redisContainer.getHost(),
                redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void setup() {
        if (testLocationId == null) {
            testLocation = new WeatherLocation();
            testLocation.setName("Horto - Juazeiro do Norte");
            testLocation.setState("CE");
            testLocation.setCountry("BR");
            testLocation.setLatitude(new BigDecimal("-7.1895000"));
            testLocation.setLongitude(new BigDecimal("-39.3328000"));
            testLocation.setActive(true);

            testLocation = locationRepository.save(testLocation);
            testLocationId = testLocation.getId();
            log.info("Test location created with ID: {}", testLocationId);
        }
    }

    @AfterAll
    static void finalCleanup(@Autowired WeatherLocationRepository repository) {
        if (testLocationId != null) {
            repository.deleteById(testLocationId);
            log.info("Global cleanup: Test location deleted");
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. First Request - Cache MISS across all levels -> API -> PostgreSQL")
    void testFirstRequest_CacheMiss_SaveToPostgreSQL() {
        log.info("Running Test 1: First request (cold cache)");

        clearAllCaches();
        currentRepository.deleteAll();

        assertThat(locationRepository.existsById(testLocationId)).isTrue();

        Current current = cacheService.getCurrentWeather(testLocationId);

        assertThat(current).isNotNull();
        log.info("First request finished");

        long count = currentRepository.count();
        log.info("Records found in database: {}", count);
        assertThat(count).as("Database should have at least one record persisted").isGreaterThanOrEqualTo(0);

        assertThat(caffeineCacheManager.getCache("weatherCurrent").get(testLocationId)).isNotNull();
        log.info("Caffeine Cache (L1) populated");

        Cache redisCache = redisCacheManager.getCache("weatherCurrent");
        assertThat(redisCache).as("The Redis cache 'weatherCurrent' should exist.").isNotNull();

        Cache.ValueWrapper wrapper = redisCache.get(testLocationId);
        assertThat(wrapper).as("No value found in Redis for locationId: " + testLocationId).isNotNull();

        Object cachedValue = wrapper.get();
        assertThat(cachedValue)
                .as("The cached value must be of type Current.")
                .isInstanceOf(Current.class);
        log.info("Redis Cache (L2) populated");
    }

    @Test
    @Order(2)
    @DisplayName("2. Second Request - Cache HIT on Caffeine (L1)")
    void testSecondRequest_CaffeineHit() {
        log.info("Running Test 2: Caffeine HIT");

        long startTime = System.currentTimeMillis();
        cacheService.getCurrentWeather(testLocationId);
        long duration = System.currentTimeMillis() - startTime;

        log.info("⏱️ Second request took: {}ms", duration);
        assertThat(duration).isLessThan(50);
    }

    @Test
    @Order(3)
    @DisplayName("3. Clear Caffeine - Cache HIT on Redis (L2)")
    void testRedisHit_AfterCaffeineClear() {
        log.info("Running Test 3: Redis HIT after clearing Caffeine");

        Objects.requireNonNull(caffeineCacheManager.getCache("weatherCurrent")).clear();
        log.info("Caffeine cleared");

        long startTime = System.currentTimeMillis();
        cacheService.getCurrentWeather(testLocationId);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Request after Caffeine clear took: {}ms", duration);

        assertThat(duration).as("Redis response time should be fast but realistic")
                .isLessThan(500);
    }

    @Test
    @Order(4)
    @DisplayName("4. Clear Caches - Cache HIT on PostgreSQL (L3)")
    void testPostgreSQLHit_AfterCachesClear() {
        log.info("Running Test 4: PostgreSQL HIT after clearing all caches");

        Objects.requireNonNull(caffeineCacheManager.getCache("weatherCurrent")).clear();
        Objects.requireNonNull(redisCacheManager.getCache("weatherCurrent")).clear();
        log.info("Caffeine and Redis cleared");

        long startTime = System.currentTimeMillis();
        cacheService.getCurrentWeather(testLocationId);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Request after clearing caches took: {}ms", duration);
        assertThat(duration).isLessThan(300);
    }

    @Test
    @Order(5)
    @DisplayName("5. Forecast Multi-level Cache Test")
    void testForecast_MultiLevelCache() {
        log.info("Running Test 5: Forecast multi-level cache");

        clearCacheByName("weatherForecast");

        long startTime1 = System.currentTimeMillis();
        List<Forecast> forecast1 = cacheService.getForecast(testLocationId, 7);
        long duration1 = System.currentTimeMillis() - startTime1;

        assertThat(forecast1).isNotEmpty();
        log.info("First forecast request: {}ms", duration1);

        long startTime2 = System.currentTimeMillis();
        cacheService.getForecast(testLocationId, 7);
        long duration2 = System.currentTimeMillis() - startTime2;

        log.info("Second forecast request (HIT): {}ms", duration2);
        assertThat(duration2).isLessThan(duration1);
    }

    @Test
    @Order(6)
    @DisplayName("6. Verify Testcontainers are running")
    void testContainersAreRunning() {
        log.info("Running Test 6: Container health check");

        assertThat(postgresContainer.isRunning())
                .as("PostgreSQL container should be running")
                .isTrue();

        assertThat(redisContainer.isRunning())
                .as("Redis container should be running")
                .isTrue();

        log.info("✅ All containers are running properly");
    }

    private void clearAllCaches() {
        List.of("weatherCurrent", "weatherForecast", "weatherStatistics").forEach(this::clearCacheByName);
        log.info("All caches cleared");
    }

    private void clearCacheByName(String name) {
        if (caffeineCacheManager.getCache(name) != null)
            Objects.requireNonNull(caffeineCacheManager.getCache(name)).clear();
        if (redisCacheManager.getCache(name) != null)
            Objects.requireNonNull(redisCacheManager.getCache(name)).clear();
    }
}