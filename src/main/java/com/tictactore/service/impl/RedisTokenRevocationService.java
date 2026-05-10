package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenRevocationService implements TokenRevocationService {

    private static final String BLOOM_FILTER_PREFIX = "jwt_denylist_bloom:";
    private static final String KEY_PREFIX = "jwt:revoked:";
    
    // We expect max 100_000 revoked tokens per day. This prevents saturation.
    private static final long EXPECTED_ELEMENTS = 100000L; 
    private static final double FALSE_POSITIVE_RATE = 0.01;
    private static final long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    private final RedissonClient redissonClient;
    private final ApplicationProperties properties;
    private final JwtService jwtService;

    private final ConcurrentHashMap<String, Boolean> initializedFilters = new ConcurrentHashMap<>();

    @Override
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        Date expirationDate;
        try {
            expirationDate = jwtService.extractExpirationDate(token);
        } catch (Exception e) {
            log.warn("Unparseable token submitted to revoke endpoint — aborting revocation");
            return;
        }

        long remainingTtlMs = expirationDate.getTime() - System.currentTimeMillis();
        if (remainingTtlMs <= 0) {
            return;
        }

        Duration tokenTtl = Duration.ofMillis(remainingTtlMs);
        long currentDay = getCurrentEpochDay();
        long expirationDay = expirationDate.getTime() / MILLIS_PER_DAY;

        try {
            for (long day = currentDay; day <= expirationDay; day++) {
                String filterName = BLOOM_FILTER_PREFIX + day;
                RBloomFilter<String> filter = redissonClient.getBloomFilter(filterName);

                long currentIterationDay = day;
                initializedFilters.computeIfAbsent(filterName, k -> {
                    boolean initialized = filter.tryInit(EXPECTED_ELEMENTS, FALSE_POSITIVE_RATE);
                    if (initialized) {
                        filter.expire(Duration.ofDays((currentIterationDay - currentDay) + 2));
                        log.info("Created new Bloom Filter: {}", filterName);
                    }
                    return true;
                });

                filter.add(token);

                if (day == currentDay) {
                    if (ThreadLocalRandom.current().nextInt(10) == 0) {
                        long count = filter.count();
                        if (count > EXPECTED_ELEMENTS * 0.8) {
                            log.warn("Bloom Filter {} is at {}% capacity", filterName, count * 100 / EXPECTED_ELEMENTS);
                        }
                    }
                }
            }

            // Add exact record to Redis as a bucket key with precise TTL
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
            RBloomFilter<String> todayFilter = redissonClient.getBloomFilter(BLOOM_FILTER_PREFIX + currentDay);
            RBloomFilter<String> yesterdayFilter = redissonClient.getBloomFilter(BLOOM_FILTER_PREFIX + (currentDay - 1));

            // Fast path: Rolling Bloom filter for current day and yesterday
            boolean inToday = todayFilter.isExists() && todayFilter.contains(token);
            boolean inYesterday = yesterdayFilter.isExists() && yesterdayFilter.contains(token);

            if (!inToday && !inYesterday) {
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
        return System.currentTimeMillis() / MILLIS_PER_DAY; // Milliseconds in a day
    }
}
