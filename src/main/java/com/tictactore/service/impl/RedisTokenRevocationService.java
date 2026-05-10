package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RedissonClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenRevocationService implements TokenRevocationService {

    private static final String BLOOM_FILTER_PREFIX = "jwt_denylist_bloom:";
    private static final String KEY_PREFIX = "jwt:revoked:";
    private static final String VALUE_REVOKED = "revoked";
    private static final String ERR_UNPARSEABLE_TOKEN = "Unparseable token submitted to revoke endpoint — aborting revocation";
    private static final String ERR_REDIS_UNAVAILABLE = "Redis unavailable during token revocation";
    private static final String LOG_CREATED_FILTER = "Created new Bloom Filter: {}";
    private static final String LOG_FILTER_CAPACITY = "Bloom Filter {} is at {}% capacity";
    private static final String LOG_TOKEN_REVOKED = "Token revoked successfully";
    private static final String LOG_REVOKE_ERROR = "Failed to revoke token due to Redis error";
    private static final String LOG_IS_REVOKED_ERROR = "Redis error checking token revocation status: fail-closed";
    
    private static final long EXPECTED_ELEMENTS = 100000L; 
    private static final double FALSE_POSITIVE_RATE = 0.01;
    private static final long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);
    private static final int WARNING_CAPACITY_PERCENTAGE = 80;
    private static final int RANDOM_LOG_CHANCE = 10;
    private static final int DAYS_TO_KEEP = 2;

    private final RedissonClient redissonClient;
    private final ApplicationProperties properties;
    private final JwtService jwtService;

    @Override
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        Date expirationDate;
        try {
            expirationDate = jwtService.extractExpirationDate(token);
        } catch (Exception e) {
            log.warn(ERR_UNPARSEABLE_TOKEN);
            return;
        }

        var remainingTtlMs = expirationDate.getTime() - System.currentTimeMillis();
        if (remainingTtlMs <= 0) {
            return;
        }

        var tokenTtl = Duration.ofMillis(remainingTtlMs);
        var currentDay = getCurrentEpochDay();
        var expirationDay = expirationDate.getTime() / MILLIS_PER_DAY;

        try {
            for (var day = currentDay; day <= expirationDay; day++) {
                var filterName = BLOOM_FILTER_PREFIX + day;
                var filter = redissonClient.<String>getBloomFilter(filterName);

                var initialized = filter.tryInit(EXPECTED_ELEMENTS, FALSE_POSITIVE_RATE);
                if (initialized) {
                    filter.expire(Duration.ofDays((day - currentDay) + DAYS_TO_KEEP));
                    log.info(LOG_CREATED_FILTER, filterName);
                }

                filter.add(token);

                if (day == currentDay) {
                    if (ThreadLocalRandom.current().nextInt(RANDOM_LOG_CHANCE) == 0) {
                        var count = filter.count();
                        if (count > EXPECTED_ELEMENTS * (WARNING_CAPACITY_PERCENTAGE / 100.0)) {
                            log.warn(LOG_FILTER_CAPACITY, filterName, count * 100 / EXPECTED_ELEMENTS);
                        }
                    }
                }
            }

            var bucket = redissonClient.<String>getBucket(KEY_PREFIX + token);
            bucket.set(VALUE_REVOKED, tokenTtl);

            log.debug(LOG_TOKEN_REVOKED);
        } catch (Exception e) {
            log.error(LOG_REVOKE_ERROR, e);
            throw new RuntimeException(ERR_REDIS_UNAVAILABLE, e);
        }
    }

    @Override
    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        var currentDay = getCurrentEpochDay();

        try {
            var todayFilter = redissonClient.<String>getBloomFilter(BLOOM_FILTER_PREFIX + currentDay);
            var yesterdayFilter = redissonClient.<String>getBloomFilter(BLOOM_FILTER_PREFIX + (currentDay - 1));

            var inToday = todayFilter.isExists() && todayFilter.contains(token);
            var inYesterday = yesterdayFilter.isExists() && yesterdayFilter.contains(token);

            if (!inToday && !inYesterday) {
                return false;
            }

            var bucket = redissonClient.<String>getBucket(KEY_PREFIX + token);
            return bucket.isExists();
        } catch (Exception e) {
            log.error(LOG_IS_REVOKED_ERROR, e);
            return true;
        }
    }

    protected long getCurrentEpochDay() {
        return System.currentTimeMillis() / MILLIS_PER_DAY;
    }
}
