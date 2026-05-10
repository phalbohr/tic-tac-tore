package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import com.tictactore.service.JwtService;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisTokenRevocationService Tests")
class RedisTokenRevocationServiceTest {

    private static final String TOKEN_TEST = "test.jwt.token";
    private static final String BLOOM_PREFIX = "jwt_denylist_bloom:";
    private static final String KEY_PREFIX = "jwt:revoked:";
    private static final String VALUE_REVOKED = "revoked";
    private static final String ERR_REDIS = "Redis connection failed";
    private static final long MOCK_EPOCH_DAY = 20000L;
    private static final long EXPECTED_ELEMENTS = 100000L;
    private static final double FALSE_POSITIVE_RATE = 0.01;
    private static final long ONE_DAY_MS = 86400000L;

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private ApplicationProperties properties;
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
    }

    @Test
    @DisplayName("Revoke Token - should add token to bloom filter and redis bucket")
    void testRevoke() {
        var expirationDate = new Date(System.currentTimeMillis() + ONE_DAY_MS);
        when(jwtService.extractExpirationDate(TOKEN_TEST)).thenReturn(expirationDate);
        
        when(redissonClient.<String>getBloomFilter(anyString())).thenReturn(todayBloomFilter);
        when(redissonClient.<String>getBucket(KEY_PREFIX + TOKEN_TEST)).thenReturn(bucket);
        when(todayBloomFilter.tryInit(EXPECTED_ELEMENTS, FALSE_POSITIVE_RATE)).thenReturn(true);

        service.revoke(TOKEN_TEST);

        verify(todayBloomFilter, atLeastOnce()).tryInit(EXPECTED_ELEMENTS, FALSE_POSITIVE_RATE);
        verify(todayBloomFilter, atLeastOnce()).expire(any(Duration.class));
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
        verify(todayBloomFilter, never()).tryInit(anyLong(), anyDouble());
        verify(yesterdayBloomFilter, never()).tryInit(anyLong(), anyDouble());
        verify(redissonClient, never()).getBucket(anyString());
    }

    @Test
    @DisplayName("Is Revoked - should return true when token in bloom filter and redis")
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
