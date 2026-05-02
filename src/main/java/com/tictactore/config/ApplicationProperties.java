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
}
