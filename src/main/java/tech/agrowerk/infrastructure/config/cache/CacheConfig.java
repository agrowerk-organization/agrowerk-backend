package tech.agrowerk.infrastructure.config.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Value("${cache.caffeine.max-size:10000}")
    private long caffeineMaxSize;

    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
        "weatherCurrent",
        "weatherForecast",
        "weatherAlerts",
        "weatherLocations",
        "activeAlerts"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(caffeineMaxSize)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.debug("Caffeine cache eviction: key={}, cause={}", key, cause)
                )
        );

        log.info("Caffeine cache manager initialized with max size: {}", caffeineMaxSize);
        return cacheManager;
    }

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(5));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("weatherCurrent", defaultConfig
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("agrowerk:weather:current:")
        );

        cacheConfigurations.put("weatherForecast", defaultConfig
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith("agrowerk:weather:forecast:")
        );

        cacheConfigurations.put("weatherAlerts", defaultConfig
                .entryTtl(Duration.ofMinutes(10))
                .prefixCacheNameWith("agrowerk:weather:alerts:")
        );

        cacheConfigurations.put("weatherLocations", defaultConfig
                .entryTtl(Duration.ofHours(24))
                .prefixCacheNameWith("agrowerk:weather:locations:")
        );

        cacheConfigurations.put("weatherStatistics", defaultConfig
                .entryTtl(Duration.ofMinutes(15))
                .prefixCacheNameWith("agrowerk:weather:stats:")
        );

        cacheConfigurations.put("activeAlerts", defaultConfig
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("agrowerk:weather:active-alerts:")
        );

        log.info("Redis cache manager initialized with {} cache configurations",
                cacheConfigurations.size());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
