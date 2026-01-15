package com.tictactore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OAuth2ConfigTest {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    public void googleClientRegistration_shouldBeLoaded() {
        ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");
        assertThat(googleRegistration).isNotNull();
        assertThat(googleRegistration.getClientId()).isNotBlank();
        assertThat(googleRegistration.getClientSecret()).isNotBlank();
    }
}
