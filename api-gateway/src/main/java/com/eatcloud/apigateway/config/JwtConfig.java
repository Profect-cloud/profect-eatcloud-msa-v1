package com.eatcloud.apigateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public Key jwtSigningKey(JwtProperties props) {
        String secret = props.getSecret();
        byte[] keyBytes = looksLikeBase64(secret) ? Decoders.BASE64.decode(secret) : secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public io.jsonwebtoken.JwtParser jwtParser(Key jwtSigningKey) {
        return Jwts.parserBuilder().setSigningKey(jwtSigningKey).build();
    }

    private boolean looksLikeBase64(String value) {
        return value.matches("[A-Za-z0-9+/=]+") && value.length() % 4 == 0;
    }
}


