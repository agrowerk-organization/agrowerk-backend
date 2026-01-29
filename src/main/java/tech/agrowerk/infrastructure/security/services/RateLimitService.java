package tech.agrowerk.infrastructure.security.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import tech.agrowerk.infrastructure.security.enums.RateLimitProfile;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();
    private final Set<String> permanentlyBlockedIps = ConcurrentHashMap.newKeySet();

    private final Cache<String, Bucket> bucketCache;

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.bucketCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(15))
                .build();

        loadBlockedIpsFromRedis();
    }

    public boolean isAllowedByIp(String ip) {
        if (isPermanentlyBlocked(ip)) {
            log.warn("IP permanently blocked attempted to access: {}", ip);
            return false;
        }

        String key = "rate:ip:" + ip;
        Bucket bucket = resolveBucket(key, RateLimitProfile.GENERAL);

        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            incrementViolationCount(ip);
        }

        return allowed;
    }

    public boolean isAllowedForSensitiveEndpoint(String ip, String endpoint) {
        if (isPermanentlyBlocked(ip)) {
            return false;
        }

        String key = "rate:sensitive:" + ip + ":" + endpoint;
        Bucket bucket = resolveBucket(key, RateLimitProfile.SENSITIVE);

        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            incrementViolationCount(ip);
            log.warn("Rate limit hit for sensitive endpoint. IP: {}, Endpoint: {}", ip, endpoint);
        }

        return allowed;
    }

    public boolean isAllowedByUser(String userId) {
        String key = "rate:user:" + userId;
        Bucket bucket = resolveBucket(key, RateLimitProfile.AUTHENTICATED);
        return bucket.tryConsume(1);
    }


    public boolean isAllowedForPublicEndpoint(String ip) {
        if (isPermanentlyBlocked(ip)) {
            return false;
        }

        String key = "rate:public:" + ip;
        Bucket bucket = resolveBucket(key, RateLimitProfile.PUBLIC);
        return bucket.tryConsume(1);
    }

    private Bucket resolveBucket(String key, RateLimitProfile profile) {
        return bucketCache.get(key, k -> createBucketForProfile(profile));
    }

    private Bucket createBucketForProfile(RateLimitProfile profile) {
        return Bucket.builder()
                .addLimit(profile.getPrimaryLimit())
                .addLimit(profile.getBurstLimit())
                .build();
    }

    private void incrementViolationCount(String ip) {
        String violationKey = "violations:" + ip;
        String countStr = redisTemplate.opsForValue().get(violationKey);

        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        count++;

        redisTemplate.opsForValue().set(violationKey, String.valueOf(count),
                Duration.ofHours(1));

        if (count >= 10 && count < 50) {
            temporaryBlock(ip, Duration.ofMinutes(15));
            log.warn("IP temporarily blocked (15min): {}", ip);
        } else if (count >= 50 && count < 100) {
            temporaryBlock(ip, Duration.ofHours(1));
            log.warn("IP temporarily blocked (1h): {}", ip);
        } else if (count >= 100) {
            permanentBlock(ip);
            log.error("IP permanently blocked: {}", ip);
        }
    }

    private void temporaryBlock(String ip, Duration duration) {
        String blockKey = "blocked:temp:" + ip;
        redisTemplate.opsForValue().set(blockKey, "blocked", duration);
    }

    private void permanentBlock(String ip) {
        permanentlyBlockedIps.add(ip);
        redisTemplate.opsForSet().add("blocked:permanent", ip);

        auditPermanentBlock(ip);
    }

    public boolean isPermanentlyBlocked(String ip) {
        if (permanentlyBlockedIps.contains(ip)) {
            return true;
        }

        String tempBlockKey = "blocked:temp:" + ip;
        if (redisTemplate.hasKey(tempBlockKey)) {
            return true;
        }

        Boolean isPermanent = redisTemplate.opsForSet().isMember("blocked:permanent", ip);
        if (Boolean.TRUE.equals(isPermanent)) {
            permanentlyBlockedIps.add(ip);
            return true;
        }

        return false;
    }

    private void loadBlockedIpsFromRedis() {
        Set<String> blockedIps = redisTemplate.opsForSet().members("blocked:permanent");
        if (blockedIps != null) {
            permanentlyBlockedIps.addAll(blockedIps);
            log.info("Loadeds {} IPs permanently blockeds", blockedIps.size());
        }
    }


    public void unblockIp(String ip) {
        permanentlyBlockedIps.remove(ip);
        redisTemplate.opsForSet().remove("blocked:permanent", ip);
        redisTemplate.delete("blocked:temp:" + ip);
        redisTemplate.delete("violations:" + ip);
        log.info("IP unlocked: {}", ip);
    }


    private void auditPermanentBlock(String ip) {
        log.error("e Audit - IP permanently blocked: {}, Timestamp: {}",
                ip, Instant.now());
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredBuckets() {
        bucketCache.cleanUp();
        log.debug("Cleanup of expired buckets performed.");
    }
}