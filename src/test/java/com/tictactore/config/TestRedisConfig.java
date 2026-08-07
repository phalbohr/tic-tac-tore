package com.tictactore.config;

import com.tictactore.service.TokenRevocationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestRedisConfig {
    @Bean
    @Primary
    public TokenRevocationService tokenRevocationService() {
        return Mockito.mock(TokenRevocationService.class);
    }
}
