package tech.agrowerk.infrastructure.security.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class IpReputationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final Set<String> SUSPICIOUS_ASN = Set.of(
            "AS16276",
            "AS14061",
            "AS16509"
    );

    public void recordSuspiciousActivity(String ip, String reason) {
        String key = "ip:suspicious:" + ip;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofDays(7));
        log.warn("Suspicious activity of IP {}: {}", ip, reason);
    }

    public int getSuspiciousActivityCount(String ip) {
        String key = "ip:suspicious:" + ip;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    public boolean isHighRiskIp(String ip) {
        return getSuspiciousActivityCount(ip) >= 10;
    }
}
