package com.eatcloud.apigateway.config;

import com.eatcloud.apigateway.filter.JwtAuthenticationFilter;
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
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	@Bean
	public io.jsonwebtoken.JwtParser jwtParser(Key jwtSigningKey) {
		return Jwts.parserBuilder().setSigningKey(jwtSigningKey).build();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter(io.jsonwebtoken.JwtParser jwtParser) {
		return new JwtAuthenticationFilter(jwtParser);
	}

	private boolean looksLikeBase64(String value) {
		return value.matches("[A-Za-z0-9+/=]+") && value.length() % 4 == 0;
	}
}