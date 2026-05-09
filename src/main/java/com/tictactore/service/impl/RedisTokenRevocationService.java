package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.service.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenRevocationService implements TokenRevocationService {

    private static final String BLOOM_FILTER_PREFIX = "jwt_denylist_bloom:";
    private static final String KEY_PREFIX = "jwt:revoked:";
    
    // We expect max 100_000 revoked tokens per day. This prevents saturation.
    private static final long EXPECTED_ELEMENTS = 100000L; 
    private static final double FALSE_POSITIVE_RATE = 0.01;

    private final RedissonClient redissonClient;
    private final ApplicationProperties properties;

    /**
     * Gets or initializes a bloom filter for a specific epoch day.
     */
    private RBloomFilter<String> getBloomFilterForDay(long epochDay) {
        String filterName = BLOOM_FILTER_PREFIX + epochDay;
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(filterName);
        boolean initialized = bloomFilter.tryInit(EXPECTED_ELEMENTS, FALSE_POSITIVE_RATE);
        if (initialized) {
            bloomFilter.expire(Duration.ofHours(48));
            log.info("Created new Bloom Filter: {}", filterName);
        }
        return bloomFilter;
    }

    @Override
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        long expirationMs = properties.getJwt().getExpiration();
        Duration tokenTtl = Duration.ofMillis(expirationMs);

        long currentDay = getCurrentEpochDay();

        try {
            // 1. Add to TODAY'S bloom filter
            RBloomFilter<String> todayFilter = getBloomFilterForDay(currentDay);
            todayFilter.add(token);
            
            long count = todayFilter.count();
            if (count > EXPECTED_ELEMENTS * 0.8) {
                log.warn("Bloom Filter {} is at {}% capacity", todayFilter.getName(), count * 100 / EXPECTED_ELEMENTS);
            }

            // 2. Add to TOMORROW'S bloom filter (in case the token's 24h lifespan crosses midnight)
            RBloomFilter<String> tomorrowFilter = getBloomFilterForDay(currentDay + 1);
            tomorrowFilter.add(token);

            // 3. Add exact record to Redis as a bucket key
            RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + token);
            bucket.set("revoked", tokenTtl);

            log.debug("Token revoked successfully");
        } catch (Exception e) {
            log.error("Failed to revoke token due to Redis error", e);
            throw new RuntimeException("Redis unavailable during token revocation", e);
        }
    }

    @Override
    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        long currentDay = getCurrentEpochDay();

        try {
            RBloomFilter<String> todayFilter = getBloomFilterForDay(currentDay);
            RBloomFilter<String> yesterdayFilter = getBloomFilterForDay(currentDay - 1);

            // Fast path: Rolling Bloom filter for current day and yesterday
            if (!todayFilter.contains(token) && !yesterdayFilter.contains(token)) {
                return false; // Definitely not in the denylist
            }

            // Slow path: Check Redis bucket directly
            RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + token);
            return bucket.isExists();
        } catch (Exception e) {
            log.error("Redis error checking token revocation status: fail-closed", e);
            // AD-03: Fail-closed logic. If Redis is down, we must reject the request
            return true;
        }
    }

    protected long getCurrentEpochDay() {
        return System.currentTimeMillis() / 86400000L; // Milliseconds in a day
    }
}
