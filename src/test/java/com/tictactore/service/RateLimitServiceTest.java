package com.tictactore.service;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.exception.RateLimitExceededException;
import com.tictactore.service.impl.RateLimitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RateLimitService Unit Tests")
class RateLimitServiceTest {

    private static final int DEFAULT_STANDALONE_LIMIT = 10;
    private static final int DEFAULT_REJECTION_THRESHOLD = 5;
    private static final int DEFAULT_REJECTION_WINDOW_HOURS = 24;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private ApplicationProperties.RateLimit rateLimitConfig;

    @Mock
    private RAtomicLong submissionCounter;

    @Mock
    @SuppressWarnings("unchecked")
    private RScoredSortedSet<String> rejectionSortedSet;

    private RateLimitServiceImpl service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        lenient().when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        lenient().when(rateLimitConfig.getStandaloneSubmissionsPerHour()).thenReturn((long) DEFAULT_STANDALONE_LIMIT);
        lenient().when(rateLimitConfig.getRejectionThreshold()).thenReturn((long) DEFAULT_REJECTION_THRESHOLD);
        lenient().when(rateLimitConfig.getRejectionWindowHours()).thenReturn((long) DEFAULT_REJECTION_WINDOW_HOURS);
        lenient().when(rateLimitConfig.getTournamentSubmissionsPerHour()).thenReturn(30L);

        service = new RateLimitServiceImpl(redissonClient, properties);
    }

    @Nested
    @DisplayName("Submission Limit Tests")
    class SubmissionLimitTests {

        @BeforeEach
        void setupSubmissionMocks() {
            doReturn(rejectionSortedSet).when(redissonClient).getScoredSortedSet(anyString(), any());
            when(rejectionSortedSet.size()).thenReturn(0);
        }

        @Test
        @DisplayName("[P0] Should allow submission when under the hourly limit")
        void shouldAllowSubmissionWhenUnderLimit() {
            when(redissonClient.getAtomicLong(anyString())).thenReturn(submissionCounter);
            when(submissionCounter.incrementAndGet()).thenReturn(1L);

            assertDoesNotThrow(() -> service.checkSubmissionLimit(userId));
            verify(submissionCounter).incrementAndGet();
        }

        @Test
        @DisplayName("[P0] Should allow submission when exactly at the limit")
        void shouldAllowSubmissionWhenExactlyAtLimit() {
            when(redissonClient.getAtomicLong(anyString())).thenReturn(submissionCounter);
            when(submissionCounter.incrementAndGet()).thenReturn((long) DEFAULT_STANDALONE_LIMIT);

            assertDoesNotThrow(() -> service.checkSubmissionLimit(userId));
            verify(submissionCounter).incrementAndGet();
        }

        @Test
        @DisplayName("[P1] Should throw RateLimitExceededException when submission count exceeds limit")
        void shouldThrowWhenSubmissionExceedsLimit() {
            when(redissonClient.getAtomicLong(anyString())).thenReturn(submissionCounter);
            when(submissionCounter.incrementAndGet()).thenReturn((long) (DEFAULT_STANDALONE_LIMIT + 1));

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSubmissionLimit(userId));
            assertFalse(ex.isRedisFailure());
            assertTrue(ex.getRetryAfterSeconds() > 0);
            assertTrue(ex.getMessage().contains("Rate limit exceeded"));
        }
    }

    @Nested
    @DisplayName("Rejection Threshold Tests")
    class RejectionThresholdTests {

        @BeforeEach
        void setupRejectionMocks() {
            doReturn(rejectionSortedSet).when(redissonClient).getScoredSortedSet(anyString(), any());
        }

        @Test
        @DisplayName("[P0] Should allow submission when rejections are under threshold")
        void shouldAllowSubmissionWhenRejectionsUnderThreshold() {
            when(rejectionSortedSet.size()).thenReturn(DEFAULT_REJECTION_THRESHOLD - 1);
            when(redissonClient.getAtomicLong(anyString())).thenReturn(submissionCounter);
            when(submissionCounter.incrementAndGet()).thenReturn(1L);

            assertDoesNotThrow(() -> service.checkSubmissionLimit(userId));
            verify(rejectionSortedSet).removeRangeByScore(anyDouble(), anyBoolean(), anyDouble(), anyBoolean());
            verify(rejectionSortedSet).size();
            verify(submissionCounter).incrementAndGet();
        }

        @Test
        @DisplayName("[P1] Should throw RateLimitExceededException when rejections meet threshold")
        void shouldThrowWhenRejectionsMeetThreshold() {
            when(rejectionSortedSet.size()).thenReturn(DEFAULT_REJECTION_THRESHOLD);

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSubmissionLimit(userId));
            assertFalse(ex.isRedisFailure());
            assertTrue(ex.getMessage().contains("rejected matches"));
        }

        @Test
        @DisplayName("[P1] Should throw RateLimitExceededException when rejections exceed threshold")
        void shouldThrowWhenRejectionsExceedThreshold() {
            when(rejectionSortedSet.size()).thenReturn(DEFAULT_REJECTION_THRESHOLD + 2);

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSubmissionLimit(userId));
            assertFalse(ex.isRedisFailure());
        }
    }

    @Nested
    @DisplayName("Redis Failure Tests")
    class RedisFailureTests {

        @Test
        @DisplayName("[P1] Should throw RateLimitExceededException with 503 semantics when Redis fails during submission check")
        void shouldThrowWithRedisFailureWhenSubmissionCheckFails() {
            doReturn(rejectionSortedSet).when(redissonClient).getScoredSortedSet(anyString(), any());
            when(rejectionSortedSet.size()).thenReturn(0);
            when(redissonClient.getAtomicLong(anyString())).thenThrow(new RedisException("Redis down"));

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSubmissionLimit(userId));
            assertTrue(ex.isRedisFailure());
            assertEquals(0, ex.getRetryAfterSeconds());
        }

        @Test
        @DisplayName("[P1] Should throw RateLimitExceededException with 503 semantics when Redis fails during rejection check")
        void shouldThrowWithRedisFailureWhenRejectionCheckFails() {
            when(redissonClient.getScoredSortedSet(anyString(), any()))
                    .thenThrow(new RedisException("Redis down"));

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSubmissionLimit(userId));
            assertTrue(ex.isRedisFailure());
            assertEquals(0, ex.getRetryAfterSeconds());
        }
    }

    @Nested
    @DisplayName("Record Rejection Tests")
    class RecordRejectionTests {

        @BeforeEach
        void setupRejectionMocks() {
            doReturn(rejectionSortedSet).when(redissonClient).getScoredSortedSet(anyString(), any());
        }

        @Test
        @DisplayName("[P0] Should add rejection entry to sorted set")
        void shouldAddRejectionEntry() {
            service.recordRejection(userId);

            verify(rejectionSortedSet).add(anyDouble(), anyString());
            verify(rejectionSortedSet).removeRangeByScore(anyDouble(), anyBoolean(), anyDouble(), anyBoolean());
            verify(rejectionSortedSet).expire(any(Duration.class));
        }

        @Test
        @DisplayName("[P1] Should not throw when Redis fails during recordRejection")
        void shouldNotThrowOnRedisFailureDuringRecord() {
            when(redissonClient.getScoredSortedSet(anyString(), any()))
                    .thenThrow(new RedisException("Redis down"));

            assertDoesNotThrow(() -> service.recordRejection(userId));
        }
    }

    @Nested
    @DisplayName("Retry-After Computation Tests")
    class RetryAfterComputationTests {

        @BeforeEach
        void setupMocks() {
            doReturn(rejectionSortedSet).when(redissonClient).getScoredSortedSet(anyString(), any());
            when(rejectionSortedSet.size()).thenReturn(0);
            when(redissonClient.getAtomicLong(anyString())).thenReturn(submissionCounter);
            when(submissionCounter.incrementAndGet()).thenReturn((long) (DEFAULT_STANDALONE_LIMIT + 1));
        }

        @Test
        @DisplayName("[P1] Should return positive retryAfterSeconds when submission count exceeds limit")
        void shouldReturnPositiveRetryAfterWhenSubmissionExceedsLimit() {
            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSubmissionLimit(userId));
            assertTrue(ex.getRetryAfterSeconds() > 0);
        }

        @Test
        @DisplayName("[P2] Should return full window retryAfter when rejection sorted set is empty during throttle")
        void shouldReturnFullWindowRetryAfterWhenSortedSetEmpty() {
            when(rejectionSortedSet.size()).thenReturn(DEFAULT_REJECTION_THRESHOLD);
            when(rejectionSortedSet.entryRange(anyDouble(), anyBoolean(), anyDouble(), anyBoolean()))
                    .thenReturn(java.util.Collections.emptyList());

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSubmissionLimit(userId));
            int expectedWindowSeconds = (int) java.util.concurrent.TimeUnit.HOURS
                    .toSeconds(DEFAULT_REJECTION_WINDOW_HOURS);
            assertThat(ex.getRetryAfterSeconds()).isEqualTo(expectedWindowSeconds);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("[P2] Should throw on rejection throttle when submission counter is also exceeded")
        void shouldThrowOnRejectionThrottleWhenSubmissionAlsoExceeded() {
            doReturn(rejectionSortedSet).when(redissonClient).getScoredSortedSet(anyString(), any());
            when(rejectionSortedSet.size()).thenReturn(DEFAULT_REJECTION_THRESHOLD);
            when(redissonClient.getAtomicLong(anyString())).thenReturn(submissionCounter);
            when(submissionCounter.incrementAndGet()).thenReturn((long) (DEFAULT_STANDALONE_LIMIT + 1));

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSubmissionLimit(userId));
            assertThat(ex.getMessage()).contains("rejected matches");
        }
    }

    @Nested
    @DisplayName("Search Rate Limit Tests")
    class SearchRateLimitTests {

        private static final int DEFAULT_SEARCH_LIMIT = 10;
        private static final String CLIENT_IP = "192.168.1.100";

        @BeforeEach
        void setUpSearchMocks() {
            when(rateLimitConfig.getSearchQueriesPerMinute()).thenReturn((long) DEFAULT_SEARCH_LIMIT);
            when(redissonClient.getAtomicLong(anyString())).thenReturn(submissionCounter);
        }

        @Test
        @DisplayName("[P0] Should allow search query when counter does not exceed threshold")
        void shouldAllowSearchQueryWhenUnderThreshold() {
            when(submissionCounter.incrementAndGet()).thenReturn(1L);

            assertDoesNotThrow(() -> service.checkSearchLimit(CLIENT_IP));
            verify(submissionCounter).expire(Duration.ofMinutes(2));
        }

        @Test
        @DisplayName("[P0] Should throw RateLimitExceededException when search limit is exceeded")
        void shouldThrowWhenSearchLimitExceeded() {
            when(submissionCounter.incrementAndGet()).thenReturn((long) (DEFAULT_SEARCH_LIMIT + 1));

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSearchLimit(CLIENT_IP));
            assertThat(ex.getMessage()).contains("too many search queries");
        }

        @Test
        @DisplayName("[P0] Should throw RateLimitExceededException with redisFailure=true when Redis throws exception")
        void shouldThrowRedisFailureWhenRedisThrowsException() {
            when(redissonClient.getAtomicLong(anyString())).thenThrow(new RedisException("Connection refused"));

            RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                    () -> service.checkSearchLimit(CLIENT_IP));
            assertThat(ex.isRedisFailure()).isTrue();
        }
    }
}
