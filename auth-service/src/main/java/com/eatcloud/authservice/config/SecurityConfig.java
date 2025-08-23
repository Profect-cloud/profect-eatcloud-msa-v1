package com.eatcloud.authservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.eatcloud.authservice.jwt.JwtAuthorizationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	private static final List<String> ALLOWED_ORIGINS = List.of(
		"http://localhost:3000",
		"http://localhost:5173",
		"http://localhost:8080"
	);

	// API endponit 허용할거 작성.
	public static final String[] PERMIT_URLS = {
		// 기본/Swagger
		"/error",
		"/favicon.ico",
		"/v3/api-docs",
		"/swagger-ui/index.html",
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/swagger-resources/**",
		"/swagger-ui.html",
		"/webjars/**",
		"/actuator/**",

		// Auth
		"/api/v1/auth/**",
		"/api/v1/auth/login",
		"/api/v1/auth/register",

		// Payment
		"/api/v1/payment/validate",
		"/api/v1/payment/confirm",
		"/api/v1/payment/status/{orderId}",
		"/api/v1/payment/checkout",
		"/api/v1/payment/success",
		"/api/v1/payment/order",
		"/api/v1/payment/fail",

		"/api/v1/payment/cancel",  // 결제 취소 엔드포인트 추가
		"/api/v1/payment/charge",

		"/api/v1/customers/**",

		"/api/v1/unauth/**"
	};

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/*
	 * 1. 로그인은 했지만 권한 부족(예: 403 Forbidden)인 경우 응답을 커스터마이징
	 * 2. OAuth2 로그인 연동(FE 와 상의필요)
	 * */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthorizationFilter jwtAuthFilter) throws
		Exception {
		//boolean isTestProfile = "test".equals(System.getProperty("spring.profiles.active"));

		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(form -> form.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PERMIT_URLS).permitAll()
						// auth-service 안에서만 접근 가능하도록 role 설정
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((req, res, e) -> {
							res.setStatus(HttpStatus.UNAUTHORIZED.value());
							res.setContentType("application/json");
							res.getWriter().write("{\"error\":\"Unauthorized\"}");
						})
						.accessDeniedHandler((req, res, e) -> {
							res.setStatus(HttpStatus.FORBIDDEN.value());
							res.setContentType("application/json");
							res.getWriter().write("{\"error\":\"Forbidden\"}");
						})
				);

		//		// ✅ 테스트가 아닐 때만 필터 등록
		//		if (!isTestProfile) {
		//			http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		//		}

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(ALLOWED_ORIGINS);
		config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		source.registerCorsConfiguration("/**", config);
		return request -> {
			String origin = request.getHeader("Origin");
			if (origin != null && !ALLOWED_ORIGINS.contains(origin)) {
				// System.out.println("CORS 차단: " + origin + "는 허용되지 않은 Origin입니다!");
				throw new RuntimeException("CORS 차단: " + origin + "는 허용되지 않은 Origin입니다!");
			}
			return config;
		};
	}

	@Bean
	public AuthenticationManager authenticationManager(
		AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}

