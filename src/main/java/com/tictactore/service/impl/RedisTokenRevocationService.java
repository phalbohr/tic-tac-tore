package com.tictactore.service.impl;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.service.TokenRevocationService;
import jakarta.annotation.PostConstruct;
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

    private static final String BLOOM_FILTER_NAME = "jwt_denylist_bloom";
    private static final String KEY_PREFIX = "jwt:revoked:";

    private final RedissonClient redissonClient;
    private final ApplicationProperties properties;
    
    private RBloomFilter<String> bloomFilter;

    @PostConstruct
    public void init() {
        bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_NAME);
        // Initialize Bloom filter with expected elements and false positive probability
        bloomFilter.tryInit(100000L, 0.01);
        log.info("Initialized Bloom Filter: {}", BLOOM_FILTER_NAME);
    }

    @Override
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        
        long expirationMs = properties.getJwt().getExpiration();
        Duration ttl = Duration.ofMillis(expirationMs);
        
        try {
            // Add to bloom filter
            bloomFilter.add(token);
            // Ensure TTL for bloom filter
            bloomFilter.expire(ttl);
            
            // Add to redis as key
            RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + token);
            bucket.set("revoked", ttl);
            
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

        try {
            // Fast path: Bloom filter
            if (!bloomFilter.contains(token)) {
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
}
