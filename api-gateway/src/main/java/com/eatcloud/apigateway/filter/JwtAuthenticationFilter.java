package com.eatcloud.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.time.Instant;


@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	private final io.jsonwebtoken.JwtParser jwtParser;

	public JwtAuthenticationFilter(io.jsonwebtoken.JwtParser jwtParser) {
		this.jwtParser = jwtParser;
	}

	@Override
	public int getOrder() {
		return -100;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getPath().value();
		String method = request.getMethod().name();

		log.debug("🔍 Processing request: {} {}", method, path);

		if ("OPTIONS".equalsIgnoreCase(method)) {
			log.debug("✅ CORS preflight request, skipping JWT validation: {} {}", method, path);
			return chain.filter(exchange);
		}

		if (isPublicPath(path)) {
			log.debug("✅ Public path detected, skipping JWT validation: {}", path);
			return chain.filter(exchange);
		}

		log.debug("🔐 Protected path, JWT validation required: {}", path);

		String token = extractToken(request);
		if (token == null) {
			log.warn("❌ Missing Authorization header for protected path: {}", path);
			return sendErrorResponse(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
		}

		try {
			Claims claims = jwtParser
				.parseClaimsJws(token)
				.getBody();

			String userId = claims.getSubject();
			String userType = claims.get("type", String.class);

			if (userId == null || userType == null) {
				log.warn("❌ Invalid token payload - missing userId or userType");
				return sendErrorResponse(exchange, "Invalid token payload", HttpStatus.UNAUTHORIZED);
			}

			log.debug("✅ JWT validation successful - User: {}, Type: {}", userId, userType);

			ServerHttpRequest modifiedRequest = request.mutate()
				.header("X-User-Id", userId)
				.header("X-User-Type", userType)
				.header("X-User-Role", "ROLE_" + userType.toUpperCase())
				.header("X-Gateway-Validated", "true")
				.build();

			return chain.filter(exchange.mutate().request(modifiedRequest).build());

		} catch (ExpiredJwtException e) {
			log.warn("❌ JWT token expired for path: {}", path);
			return sendErrorResponse(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
		} catch (MalformedJwtException e) {
			log.warn("❌ Malformed JWT token for path: {}", path);
			return sendErrorResponse(exchange, "Malformed token", HttpStatus.UNAUTHORIZED);
		} catch (SignatureException e) {
			log.warn("❌ Invalid JWT signature for path: {}", path);
			return sendErrorResponse(exchange, "Invalid token signature", HttpStatus.UNAUTHORIZED);
		} catch (UnsupportedJwtException e) {
			log.warn("❌ Unsupported JWT token for path: {}", path);
			return sendErrorResponse(exchange, "Unsupported token", HttpStatus.UNAUTHORIZED);
		} catch (IllegalArgumentException e) {
			log.warn("❌ Illegal JWT token for path: {}", path);
			return sendErrorResponse(exchange, "Illegal token", HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			log.error("❌ Unexpected error during JWT validation for path: " + path, e);
			return sendErrorResponse(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
		}
	}


	private boolean isPublicPath(String path) {
		if (path.endsWith("/health") ||
			path.contains("/health/") ||
			path.contains("/actuator")) {
			return true;
		}

		if (path.startsWith("/api/v1/auth/") ||
			path.startsWith("/api/auth/") ||
			path.startsWith("/auth/") ||
			path.startsWith("/oauth2/")) {
			return true;
		}

		if (path.startsWith("/dev/")) {
			return true;
		}

		// Legacy Swagger paths (for backward compatibility)
		if (path.startsWith("/swagger-ui") ||
			path.startsWith("/v3/api-docs") ||
			path.startsWith("/swagger-resources/") ||
			path.startsWith("/webjars/")) {
			return true;
		}

		// Service-specific paths (simplified)
		if (path.startsWith("/user-service/") ||
			path.startsWith("/order-service/") ||
			path.startsWith("/store-service/") ||
			path.startsWith("/payment-service/")) {
			return true;
		}

		return path.equals("/favicon.ico") || path.equals("/error");
	}


	private String extractToken(ServerHttpRequest request) {
		String authHeader = request.getHeaders().getFirst("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}
		return null;
	}


	private Mono<Void> sendErrorResponse(ServerWebExchange exchange, String message, HttpStatus status) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

		String errorBody = String.format(
			"{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\",\"path\":\"%s\"}",
			status.getReasonPhrase(),
			message,
			Instant.now().toString(),
			exchange.getRequest().getPath().value()
		);

		DataBuffer buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
		return response.writeWith(Mono.just(buffer));
	}
}