package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import com.tictactore.service.JwtService;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisTokenRevocationServiceTest {

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

    private static final long MOCK_EPOCH_DAY = 20000L;

    // We extend the service to control the concept of "current time/day" for predictability in tests.
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
    void testRevoke() {
        String token = "test.jwt.token";
        
        Date expirationDate = new Date(System.currentTimeMillis() + 86400000L); // 24h
        when(jwtService.extractExpirationDate(token)).thenReturn(expirationDate);
        
        String todayFilterName = "jwt_denylist_bloom:" + MOCK_EPOCH_DAY;
        long expirationDay = expirationDate.getTime() / 86400000L;

        when(redissonClient.<String>getBloomFilter(anyString())).thenReturn(todayBloomFilter);
        when(redissonClient.<String>getBucket("jwt:revoked:" + token)).thenReturn(bucket);
        when(todayBloomFilter.tryInit(100000L, 0.01)).thenReturn(true);

        service.revoke(token);

        verify(todayBloomFilter, atLeastOnce()).tryInit(100000L, 0.01);
        verify(todayBloomFilter, atLeastOnce()).expire(any(java.time.Duration.class));
        verify(todayBloomFilter, atLeastOnce()).add(token);

        verify(bucket).set(eq("revoked"), any(java.time.Duration.class));
    }

    @Test
    void testIsRevoked_whenNotInBloomFilter() {
        String token = "test.jwt.token";
        String todayFilterName = "jwt_denylist_bloom:" + MOCK_EPOCH_DAY;
        String yesterdayFilterName = "jwt_denylist_bloom:" + (MOCK_EPOCH_DAY - 1);
        
        when(redissonClient.<String>getBloomFilter(todayFilterName)).thenReturn(todayBloomFilter);
        when(redissonClient.<String>getBloomFilter(yesterdayFilterName)).thenReturn(yesterdayBloomFilter);
        when(todayBloomFilter.isExists()).thenReturn(true);
        when(todayBloomFilter.contains(token)).thenReturn(false);
        when(yesterdayBloomFilter.isExists()).thenReturn(true);
        when(yesterdayBloomFilter.contains(token)).thenReturn(false);

        boolean result = service.isRevoked(token);

        assertFalse(result);
        verify(todayBloomFilter, never()).tryInit(anyLong(), anyDouble());
        verify(yesterdayBloomFilter, never()).tryInit(anyLong(), anyDouble());
        verify(redissonClient, never()).getBucket(anyString());
    }

    @Test
    void testIsRevoked_whenInBloomFilterAndInRedis() {
        String token = "test.jwt.token";
        String todayFilterName = "jwt_denylist_bloom:" + MOCK_EPOCH_DAY;
        String yesterdayFilterName = "jwt_denylist_bloom:" + (MOCK_EPOCH_DAY - 1);

        when(redissonClient.<String>getBloomFilter(todayFilterName)).thenReturn(todayBloomFilter);
        when(redissonClient.<String>getBloomFilter(yesterdayFilterName)).thenReturn(yesterdayBloomFilter);
        // Let's say it's not in today's, but it's in yesterday's filter
        when(todayBloomFilter.isExists()).thenReturn(true);
        when(todayBloomFilter.contains(token)).thenReturn(false);
        when(yesterdayBloomFilter.isExists()).thenReturn(true);
        when(yesterdayBloomFilter.contains(token)).thenReturn(true);
        when(redissonClient.<String>getBucket("jwt:revoked:" + token)).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(true);

        boolean result = service.isRevoked(token);

        assertTrue(result);
    }

    @Test
    void testIsRevoked_whenInBloomFilterButNotInRedis() {
        String token = "test.jwt.token";
        String todayFilterName = "jwt_denylist_bloom:" + MOCK_EPOCH_DAY;
        String yesterdayFilterName = "jwt_denylist_bloom:" + (MOCK_EPOCH_DAY - 1);

        when(redissonClient.<String>getBloomFilter(todayFilterName)).thenReturn(todayBloomFilter);
        when(redissonClient.<String>getBloomFilter(yesterdayFilterName)).thenReturn(yesterdayBloomFilter);
        // Let's say it's in today's filter
        when(todayBloomFilter.isExists()).thenReturn(true);
        when(todayBloomFilter.contains(token)).thenReturn(true);
        when(redissonClient.<String>getBucket("jwt:revoked:" + token)).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(false);

        boolean result = service.isRevoked(token);

        assertFalse(result); // False positive scenario
    }

    @Test
    void testIsRevoked_failClosed_whenRedisDown() {
        String token = "test.jwt.token";

        when(redissonClient.<String>getBloomFilter(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        boolean result = service.isRevoked(token);

        assertTrue(result); // System fails closed securely
    }
}
