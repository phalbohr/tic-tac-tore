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
    private String publicKey = "BE_jfHrmkFYm52tuVzmcTbD2KvuUg1uFGGbZAR9Y_8Ha6V8SeyH8UJG-h6nad1za6C6T1uUEyCCGIyP35waNJa0";
    private String privateKey = "hy6yBC2PIX-WPm2Egq6abCRtNttqdCRz4mdoMBWTHj0";
    private String subject = "mailto:admin@tictactore.com";
}
