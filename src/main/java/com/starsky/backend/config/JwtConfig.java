package com.starsky.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    public String getSecret() {
        return secret;
    }

    public String getTokenPrefix() {
        return "Bearer ";
    }

    public String getAuthorizationHeader() {
        return "Authorization";
    }

    public Duration getExpirationTime() {
        return Duration.ofDays(1);
    }

    public String getRegisterUrl() {
        return "/users";
    }
}
