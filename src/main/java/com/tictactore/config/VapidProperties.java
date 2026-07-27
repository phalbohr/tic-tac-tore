package com.tictactore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.security.vapid")
@Getter
@Setter
public class VapidProperties {
    private String publicKey = "BEl62iUYgUivxIkv69yViEuiBIa40yYvrx1m0A7Vn65a7p5y_dummy_public_key_string_for_testing_purposes";
    private String privateKey = "dummy_private_key_string_for_testing_purposes";
    private String subject = "mailto:admin@tictactore.com";
}
