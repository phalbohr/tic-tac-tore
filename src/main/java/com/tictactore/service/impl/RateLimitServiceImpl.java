package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.exception.RateLimitExceededException;
import com.tictactore.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private static final String SUBMISSIONS_KEY_PREFIX = "rl:submissions:";
    private static final String REJECTIONS_KEY_PREFIX = "rl:rejections:";
    private static final String HOUR_FORMATTER = "yyyy-MM-dd-HH";
    private static final int SUBMISSION_TTL_HOURS = 2;

    private final RedissonClient redissonClient;
    private final ApplicationProperties properties;

    @Override
    public void checkSubmissionLimit(UUID userId) {
        checkRejectionLimit(userId);
        checkSubmissionCounter(userId);
    }

    private void checkRejectionLimit(UUID userId) {
        var rateLimitProps = properties.getRateLimit();
        long threshold = rateLimitProps.getRejectionThreshold();
        long windowHours = rateLimitProps.getRejectionWindowHours();

        String key = REJECTIONS_KEY_PREFIX + userId;

        try {
            RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key, StringCodec.INSTANCE);
            long now = System.currentTimeMillis();
            long windowMs = TimeUnit.HOURS.toMillis(Math.toIntExact(windowHours));
            long oldestAllowed = now - windowMs;

            sortedSet.removeRangeByScore(0.0, true, (double) oldestAllowed, true);

            long count = sortedSet.size();
            if (count >= threshold) {
                int retryAfter = computeRejectionRetryAfter(sortedSet, windowMs, now);
                throw new RateLimitExceededException(
                        retryAfter,
                        "Rate limit exceeded: too many rejected matches. Please try again later."
                );
            }

            if (count > 0) {
                sortedSet.expire(Duration.ofHours(Math.toIntExact(windowHours)));
            }
        } catch (RedisException e) {
            log.error("Redis unavailable during rejection rate-limit check for user {}", userId, e);
            throw new RateLimitExceededException(
                    "Redis unavailable during rate-limit check", e
            );
        }
    }

    private int computeRejectionRetryAfter(RScoredSortedSet<String> sortedSet, long windowMs, long now) {
        try {
            Collection<ScoredEntry<String>> entries = sortedSet.entryRange(0.0, true, Double.POSITIVE_INFINITY, true);
            if (entries != null && !entries.isEmpty()) {
                ScoredEntry<String> oldest = entries.iterator().next();
                long oldestTimestamp = (long) (double) oldest.getScore();
                long retryAfterMs = oldestTimestamp + windowMs - now;
                return retryAfterMs > 0 ? (int) (retryAfterMs / 1000) : 0;
            }
        } catch (RedisException e) {
            log.warn("Failed to compute rejection retry-after, falling back to full window", e);
        }
        return (int) (windowMs / 1000);
    }

    private void checkSubmissionCounter(UUID userId) {
        var rateLimitProps = properties.getRateLimit();
        long threshold = rateLimitProps.getStandaloneSubmissionsPerHour();

        String hourKey = getCurrentHourKey();
        String key = SUBMISSIONS_KEY_PREFIX + userId + ":" + hourKey;

        try {
            RAtomicLong counter = redissonClient.getAtomicLong(key);
            long count = counter.incrementAndGet();
            counter.expire(Duration.ofHours(SUBMISSION_TTL_HOURS));

            if (count > threshold) {
                int retryAfter = computeSubmissionRetryAfter();
                throw new RateLimitExceededException(
                        retryAfter,
                        "Rate limit exceeded: too many match submissions. Please try again in "
                                + retryAfter + " seconds."
                );
            }
        } catch (RedisException e) {
            log.error("Redis unavailable during submission rate-limit check for user {}", userId, e);
            throw new RateLimitExceededException(
                    "Redis unavailable during rate-limit check", e
            );
        }
    }

    private int computeSubmissionRetryAfter() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime startOfNextHour = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        return (int) Duration.between(now, startOfNextHour).getSeconds();
    }

    private String getCurrentHourKey() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(HOUR_FORMATTER));
    }

    @Override
    public void recordRejection(UUID userId) {
        var rateLimitProps = properties.getRateLimit();
        long windowHours = rateLimitProps.getRejectionWindowHours();

        String key = REJECTIONS_KEY_PREFIX + userId;

        try {
            RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key, StringCodec.INSTANCE);
            long now = System.currentTimeMillis();
            sortedSet.add((double) now, UUID.randomUUID().toString());
            sortedSet.removeRangeByScore(0.0, true, (double) (now - TimeUnit.HOURS.toMillis(Math.toIntExact(windowHours))), true);
            sortedSet.expire(Duration.ofHours(Math.toIntExact(windowHours)));
        } catch (RedisException e) {
            log.warn("Redis unavailable while recording rejection for user {}", userId, e);
        }
    }
}
