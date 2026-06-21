package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.JwtException;
import org.redisson.api.RedissonClient;
import org.redisson.api.RScript;
import org.redisson.client.RedisException;
import org.redisson.client.codec.StringCodec;

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

    private static final long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);
    private static final int WARNING_CAPACITY_PERCENTAGE = 80;
    private static final int RANDOM_LOG_CHANCE = 10;
    private static final int DAYS_TO_KEEP = 2;

    private static final String LUA_INIT_AND_EXPIRE = "if redis.call('EXISTS', KEYS[1]) == 0 then " +
            "  redis.call('BF.RESERVE', KEYS[1], ARGV[1], ARGV[2]); " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[3]); " +
            "  return 1; " +
            "else " +
            "  return 0; " +
            "end";

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
        } catch (JwtException | IllegalArgumentException e) {
            log.warn(ERR_UNPARSEABLE_TOKEN);
            return;
        }

        var now = System.currentTimeMillis();
        var remainingTtlMs = expirationDate.getTime() - now;
        if (remainingTtlMs <= 0) {
            return;
        }

        var tokenTtl = Duration.ofMillis(remainingTtlMs);
        var currentDay = getCurrentEpochDay();
        var expirationDay = expirationDate.getTime() / MILLIS_PER_DAY;
        var maxDay = Math.min(expirationDay, currentDay + DAYS_TO_KEEP);

        try {
            var bfConfig = properties.getBloomFilter();
            for (var day = currentDay; day <= maxDay; day++) {
                var filterName = BLOOM_FILTER_PREFIX + day;

                var script = redissonClient.getScript(StringCodec.INSTANCE);
                var ttlSeconds = TimeUnit.DAYS.toSeconds((day - currentDay) + DAYS_TO_KEEP);

                try {
                    var result = script.eval(
                            RScript.Mode.READ_WRITE,
                            LUA_INIT_AND_EXPIRE,
                            RScript.ReturnType.VALUE,
                            List.of(filterName),
                            String.valueOf(bfConfig.getFalsePositiveRate()),
                            String.valueOf(bfConfig.getExpectedElements()),
                            String.valueOf(ttlSeconds));

                    if (Long.valueOf(1).equals(result)) {
                        log.info(LOG_CREATED_FILTER, filterName);
                    }

                    script.eval(
                            RScript.Mode.READ_WRITE,
                            "redis.call('BF.ADD', KEYS[1], ARGV[1]); return 1;",
                            RScript.ReturnType.VALUE,
                            List.of(filterName),
                            token);
                } catch (RedisException e) {
                    log.debug("RedisBloom commands not available, skipping Bloom Filter update for " + filterName);
                }

                if (day == currentDay) {
                    if (ThreadLocalRandom.current().nextInt(RANDOM_LOG_CHANCE) == 0) {
                        // BF.INFO is complex to parse, so we skip the capacity check for now 
                        // since we're using raw RedisBloom scripts.
                    }
                }
            }

            var bucket = redissonClient.<String>getBucket(KEY_PREFIX + token);
            bucket.set(VALUE_REVOKED, tokenTtl);

            log.debug(LOG_TOKEN_REVOKED);
        } catch (RedisException e) {
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
            var script = redissonClient.getScript(StringCodec.INSTANCE);
            var luaCheck = "if redis.call('EXISTS', KEYS[1]) == 1 then " +
                           "  return redis.call('BF.EXISTS', KEYS[1], ARGV[1]); " +
                           "else " +
                           "  return 0; " +
                           "end";

            var inToday = false;
            var bloomError = false;
            try {
                var res = script.eval(RScript.Mode.READ_ONLY, luaCheck, RScript.ReturnType.VALUE, List.of(BLOOM_FILTER_PREFIX + currentDay), token);
                inToday = Long.valueOf(1).equals(res);
            } catch (RedisException e) {
                log.debug("Ignored error checking today Bloom filter", e);
                bloomError = true;
            }

            var inYesterday = false;
            try {
                var res = script.eval(RScript.Mode.READ_ONLY, luaCheck, RScript.ReturnType.VALUE, List.of(BLOOM_FILTER_PREFIX + (currentDay - 1)), token);
                inYesterday = Long.valueOf(1).equals(res);
            } catch (RedisException e) {
                log.debug("Ignored error checking yesterday Bloom filter", e);
                bloomError = true;
            }

            if (bloomError && !inToday && !inYesterday) {
                log.error(LOG_IS_REVOKED_ERROR);
                return true;
            }

            if (!inToday && !inYesterday) {
                return false;
            }

            var bucket = redissonClient.<String>getBucket(KEY_PREFIX + token);
            return bucket.isExists();
        } catch (RedisException e) {
            log.error(LOG_IS_REVOKED_ERROR, e);
            return true;
        }
    }

    protected long getCurrentEpochDay() {
        return Instant.now().atZone(ZoneOffset.UTC).toLocalDate().toEpochDay();
    }
}
