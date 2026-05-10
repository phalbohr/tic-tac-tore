package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;

import com.tictactore.service.JwtService;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RedisTokenRevocationService Tests")
class RedisTokenRevocationServiceTest {

    private static final String TOKEN_TEST = "test.jwt.token";
    private static final String BLOOM_PREFIX = "jwt_denylist_bloom:";
    private static final String KEY_PREFIX = "jwt:revoked:";
    private static final String VALUE_REVOKED = "revoked";
    private static final String ERR_REDIS = "Redis connection failed";
    private static final long MOCK_EPOCH_DAY = java.time.Instant.now().atZone(java.time.ZoneOffset.UTC).toLocalDate().toEpochDay();
    private static final long EXPECTED_ELEMENTS = 100000L;
    private static final double FALSE_POSITIVE_RATE = 0.01;
    private static final long ONE_DAY_MS = 86400000L;

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private ApplicationProperties properties;
    @Mock
    private ApplicationProperties.BloomFilter bloomFilterConfig;
    @Mock
    private ApplicationProperties.Jwt jwtProperties;
    @Mock
    private JwtService jwtService;
    
    @Mock
    private RBloomFilter<String> todayBloomFilter;
    @Mock
    private RBloomFilter<String> tomorrowBloomFilter;
    @Mock
    private RBloomFilter<String> yesterdayBloomFilter;
    
    @Mock
    private RBucket<String> bucket;

    @Mock
    private RScript script;

    private TestableRedisTokenRevocationService service;

    private class TestableRedisTokenRevocationService extends RedisTokenRevocationService {
        public TestableRedisTokenRevocationService(RedissonClient redissonClient, ApplicationProperties properties, JwtService jwtService) {
            super(redissonClient, properties, jwtService);
        }

        @Override
        protected long getCurrentEpochDay() {
            return MOCK_EPOCH_DAY;
        }
    }
@BeforeEach
void setUp() {
    service = new TestableRedisTokenRevocationService(redissonClient, properties, jwtService);
    when(properties.getBloomFilter()).thenReturn(bloomFilterConfig);
    when(bloomFilterConfig.getExpectedElements()).thenReturn(EXPECTED_ELEMENTS);
    when(bloomFilterConfig.getFalsePositiveRate()).thenReturn(FALSE_POSITIVE_RATE);

    doReturn(script).when(redissonClient).getScript();
}

@Test
@DisplayName("Revoke Token - should add token to bloom filter and redis bucket")
void testRevoke() {
    var expirationDate = new Date(System.currentTimeMillis() + ONE_DAY_MS);
    when(jwtService.extractExpirationDate(TOKEN_TEST)).thenReturn(expirationDate);

    when(script.eval(any(), anyString(), any(), anyList(), any(), any(), any())).thenReturn(1L);

    when(redissonClient.<String>getBloomFilter(anyString())).thenReturn(todayBloomFilter);
    when(redissonClient.<String>getBucket(KEY_PREFIX + TOKEN_TEST)).thenReturn(bucket);

    service.revoke(TOKEN_TEST);

    verify(redissonClient, atLeastOnce()).getScript();
    verify(script, atLeastOnce()).eval(any(), anyString(), any(), anyList(), any(), any(), any());
    verify(todayBloomFilter, atLeastOnce()).add(TOKEN_TEST);
    verify(bucket).set(eq(VALUE_REVOKED), any(Duration.class));
}

    @Test
    @DisplayName("Is Revoked - should return false when token not in bloom filter")
    void testIsRevoked_whenNotInBloomFilter() {
        var todayFilterName = BLOOM_PREFIX + MOCK_EPOCH_DAY;
        var yesterdayFilterName = BLOOM_PREFIX + (MOCK_EPOCH_DAY - 1);
        
        when(redissonClient.<String>getBloomFilter(todayFilterName)).thenReturn(todayBloomFilter);
        when(redissonClient.<String>getBloomFilter(yesterdayFilterName)).thenReturn(yesterdayBloomFilter);
        when(todayBloomFilter.isExists()).thenReturn(true);
        when(todayBloomFilter.contains(TOKEN_TEST)).thenReturn(false);
        when(yesterdayBloomFilter.isExists()).thenReturn(true);
        when(yesterdayBloomFilter.contains(TOKEN_TEST)).thenReturn(false);

        var result = service.isRevoked(TOKEN_TEST);

        assertFalse(result);
        verify(redissonClient, never()).getBucket(anyString());
    }

    @Test
    @DisplayName("Is Revoked - should return true when token in bloom filter and in redis")
    void testIsRevoked_whenInBloomFilterAndInRedis() {
        var todayFilterName = BLOOM_PREFIX + MOCK_EPOCH_DAY;
        var yesterdayFilterName = BLOOM_PREFIX + (MOCK_EPOCH_DAY - 1);

        when(redissonClient.<String>getBloomFilter(todayFilterName)).thenReturn(todayBloomFilter);
        when(redissonClient.<String>getBloomFilter(yesterdayFilterName)).thenReturn(yesterdayBloomFilter);
        when(todayBloomFilter.isExists()).thenReturn(true);
        when(todayBloomFilter.contains(TOKEN_TEST)).thenReturn(false);
        when(yesterdayBloomFilter.isExists()).thenReturn(true);
        when(yesterdayBloomFilter.contains(TOKEN_TEST)).thenReturn(true);
        when(redissonClient.<String>getBucket(KEY_PREFIX + TOKEN_TEST)).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(true);

        var result = service.isRevoked(TOKEN_TEST);

        assertTrue(result);
    }

    @Test
    @DisplayName("Is Revoked - should return false on false positive (in bloom but not redis)")
    void testIsRevoked_whenInBloomFilterButNotInRedis() {
        var todayFilterName = BLOOM_PREFIX + MOCK_EPOCH_DAY;
        var yesterdayFilterName = BLOOM_PREFIX + (MOCK_EPOCH_DAY - 1);

        when(redissonClient.<String>getBloomFilter(todayFilterName)).thenReturn(todayBloomFilter);
        when(redissonClient.<String>getBloomFilter(yesterdayFilterName)).thenReturn(yesterdayBloomFilter);
        when(todayBloomFilter.isExists()).thenReturn(true);
        when(todayBloomFilter.contains(TOKEN_TEST)).thenReturn(true);
        when(redissonClient.<String>getBucket(KEY_PREFIX + TOKEN_TEST)).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(false);

        var result = service.isRevoked(TOKEN_TEST);

        assertFalse(result);
    }

    @Test
    @DisplayName("Is Revoked - should fail closed (return true) when Redis is down")
    void testIsRevoked_failClosed_whenRedisDown() {
        when(redissonClient.<String>getBloomFilter(anyString())).thenThrow(new RuntimeException(ERR_REDIS));

        var result = service.isRevoked(TOKEN_TEST);

        assertTrue(result);
    }
}
