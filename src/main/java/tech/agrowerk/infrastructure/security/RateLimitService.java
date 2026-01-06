package tech.agrowerk.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import org.springframework.stereotype.Component;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
