package tech.agrowerk.infrastructure.security.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;


    public void blacklistToken(String token, long expirationSeconds) {
        String key = "blacklist:token:" + token;
        redisTemplate.opsForValue().set(key, "revoked", expirationSeconds, TimeUnit.SECONDS);
        log.info("Token added to blacklist");
    }


    public boolean isBlacklisted(String token) {
        String key = "blacklist:token:" + token;
        return redisTemplate.hasKey(key);
    }


    public void blacklistAllUserTokens(Long userId) {
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

    public boolean isTokenIssuedBeforeUserBlacklist(Long userId, Instant tokenIssuedAt) {
        String key = "blacklist:user:" + userId;
        String blacklistTimestamp = redisTemplate.opsForValue().get(key);

        if (blacklistTimestamp == null) {
            return false;
        }

        Instant blacklistTime = Instant.ofEpochMilli(Long.parseLong(blacklistTimestamp));
        return tokenIssuedAt.isBefore(blacklistTime);
    }
}