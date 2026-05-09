package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
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
    private RBloomFilter<String> bloomFilter;
    @Mock
    private RBucket<String> bucket;

    @InjectMocks
    private RedisTokenRevocationService service;

    @BeforeEach
    void setUp() {
        when(redissonClient.<String>getBloomFilter(anyString())).thenReturn(bloomFilter);
        service.init();
    }

    @Test
    void testRevoke() {
        String token = "test.jwt.token";
        when(properties.getJwt()).thenReturn(jwtProperties);
        when(jwtProperties.getExpiration()).thenReturn(86400000L);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);

        service.revoke(token);

        verify(bloomFilter).add(token);
        verify(bucket).set(eq("revoked"), any());
    }

    @Test
    void testIsRevoked_whenNotInBloomFilter() {
        String token = "test.jwt.token";
        when(bloomFilter.contains(token)).thenReturn(false);

        boolean result = service.isRevoked(token);

        assertFalse(result);
        verify(redissonClient, never()).getBucket(anyString());
    }

    @Test
    void testIsRevoked_whenInBloomFilterAndInRedis() {
        String token = "test.jwt.token";
        when(bloomFilter.contains(token)).thenReturn(true);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(true);

        boolean result = service.isRevoked(token);

        assertTrue(result);
    }

    @Test
    void testIsRevoked_whenInBloomFilterButNotInRedis() {
        String token = "test.jwt.token";
        when(bloomFilter.contains(token)).thenReturn(true);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(false);

        boolean result = service.isRevoked(token);

        assertFalse(result);
    }

    @Test
    void testIsRevoked_failClosed_whenRedisDown() {
        String token = "test.jwt.token";
        when(bloomFilter.contains(token)).thenThrow(new RuntimeException("Redis connection failed"));

        boolean result = service.isRevoked(token);

        assertTrue(result);
    }
}