package com.tictactore.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuleConfigurationInitializer Unit Tests")
class RuleConfigurationInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RuleConfigurationInitializer initializer;

    @Test
    @DisplayName("initializePresets should execute SQL insert queries for presets")
    void shouldExecuteSqlInserts() {
        initializer.initializePresets();

        verify(jdbcTemplate, times(2)).update(anyString());
    }
}
