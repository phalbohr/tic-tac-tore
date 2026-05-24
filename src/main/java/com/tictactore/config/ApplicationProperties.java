package com.tictactore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.security")
public class ApplicationProperties {

    private final Jwt jwt = new Jwt();
    private final OAuth2 oauth2 = new OAuth2();
    private final BloomFilter bloomFilter = new BloomFilter();
    private final Avatar avatar = new Avatar();

    @Getter
    @Setter
    public static class Jwt {
        private String secretKey;
        private long expiration;
    }

    @Getter
    @Setter
    public static class OAuth2 {
        private String redirectUri;
    }

    @Getter
    @Setter
    public static class BloomFilter {
        private long expectedElements = 100000L;
        private double falsePositiveRate = 0.01;
    }

    @Getter
    @Setter
    public static class Avatar {
        private String apiUrl = "https://api.dicebear.com/7.x/identicon/svg?seed=";
        private String salt;
    }
}
