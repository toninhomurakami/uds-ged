package br.com.uds.tools.ged.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ged.jwt")
@Data
public class JwtProperties {

    private String secret = "ged-secret-key-change-in-production-min-256-bits";
    private long expirationMs = 3600000;  // 1 hora
}
