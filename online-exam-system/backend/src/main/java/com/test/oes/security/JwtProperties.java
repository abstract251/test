package com.test.oes.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class JwtProperties {
    private Jwt jwt = new Jwt();
    private String[] allowedOriginPatterns = new String[0];

    @Getter
    @Setter
    public static class Jwt {
        private String issuer;
        private String secret;
        private long accessTokenMinutes;
        private long refreshTokenDays;
    }
}
