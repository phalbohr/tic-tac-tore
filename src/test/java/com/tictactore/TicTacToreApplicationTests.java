package com.tictactore;

import com.tictactore.service.TokenRevocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.redisson.api.RedissonClient;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TicTacToreApplication Tests")
class TicTacToreApplicationTests {

    @MockBean
    private RedissonClient redissonClient;
    
    @MockBean
    private TokenRevocationService tokenRevocationService;

    @Test
    @DisplayName("Context Loads - should verify Spring application context starts successfully")
    void contextLoads() {
    }
}
