package com.tictactore.config;

import com.tictactore.security.CsrfCookieFilter;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;

    @Mock
    private JwtAuthenticationFilter jwtAuthFilter;

    @Mock
    private CsrfCookieFilter csrfCookieFilter;

    @Mock
    private Environment env;

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins",
                List.of("http://localhost:3000", "https://prod.com"));
    }

    @Test
    @DisplayName("corsConfigurationSource - should configure allowedOriginPatterns instead of allowedOrigins for production credentials safety")
    void shouldConfigureCorsWithOriginPatterns() {
        var source = securityConfig.corsConfigurationSource();
        var request = new MockHttpServletRequest();
        request.setMethod("GET");

        var config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).isNullOrEmpty();
        assertThat(config.getAllowedOriginPatterns()).containsExactlyInAnyOrder("http://localhost:3000",
                "https://prod.com");
        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH");
        assertThat(config.getAllowedHeaders()).contains("Authorization", "Cache-Control", "Content-Type",
                "X-XSRF-TOKEN", "X-Requested-With", "Accept", "Origin");
    }
}
