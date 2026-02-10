package tech.agrowerk.infrastructure.security.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    @CacheEvict(value = "tokenBlacklist", key = "#token")
    public void blacklistToken(String token, long expirationSeconds) {
        String key = "blacklist:token:" + token;
        redisTemplate.opsForValue().set(key, "revoked", expirationSeconds, TimeUnit.SECONDS);
        log.info("Token added to blacklist");
    }


    @Cacheable(value = "tokenBlacklist", key = "#token", cacheManager = "caffeineCacheManager")
    public boolean isBlacklisted(String token) {
        String key = "blacklist:token:" + token;
        return redisTemplate.hasKey(key);
    }


    public void blacklistAllUserTokens(UUID userId) {
        String pattern = "blacklist:user:" + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        String userBlacklistKey = "blacklist:user:" + userId;
        redisTemplate.opsForValue().set(
                userBlacklistKey,
                String.valueOf(Instant.now().toEpochMilli()),
                Duration.ofDays(30)
        );
        log.warn("All of the user's {} tokens have invalidated", userId);
    }

    @Cacheable(value = "userBlacklist", key = "#userId", cacheManager = "caffeineCacheManager")
    public boolean isTokenIssuedBeforeUserBlacklist(UUID userId, Instant tokenIssuedAt) {
        String key = "blacklist:user:" + userId;
        String blacklistTimestamp = redisTemplate.opsForValue().get(key);

        if (blacklistTimestamp == null) {
            return false;
        }

        Instant blacklistTime = Instant.ofEpochMilli(Long.parseLong(blacklistTimestamp));
        return tokenIssuedAt.isBefore(blacklistTime);
    }

    @CacheEvict(value = "userBlacklist", key = "#userId")
    public void evictUserBlacklistCache(UUID userId) {
        log.debug("Evicting user blacklist cache for {}", userId);
    }
}