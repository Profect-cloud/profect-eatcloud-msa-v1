package com.eatcloud.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GatewayConfig {

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		log.info("🚀 Configuring Gateway Routes...");

		return builder.routes()
			.route("auth-service", r -> r
				.path("/api/v1/auth/**")
				.filters(f -> f
					// 인증 경로는 user-service 컨트롤러 매핑(`/api/v1/auth/**`)과 일치시키기 위해 prefix 제거 안 함
					.addRequestHeader("X-Service-Name", "auth-service"))
				.uri("lb://user-service"))

			.route("user-service", r -> r
				.path("/api/v1/customers/**", "/api/v1/manager/**", "/api/v1/admin/**")
				.filters(f -> f
					.stripPrefix(2)
					.addRequestHeader("X-Service-Name", "user-service"))
				.uri("lb://user-service"))

            // User Service Swagger UI - forwarding with forwarded prefix for springdoc
            .route("user-service-swagger", r -> r
                .path("/user-service/**")
                .filters(f -> f
                    .rewritePath("/user-service/(?<segment>.*)", "/${segment}")
                    .addRequestHeader("X-Forwarded-Prefix", "/user-service")
                    .addRequestHeader("X-Service-Name", "user-service"))
                .uri("lb://user-service"))

			// Order Service Swagger UI - Simple forwarding
			.route("order-service-swagger", r -> r
				.path("/order-service/**")
				.filters(f -> f
					.rewritePath("/order-service/(?<segment>.*)", "/${segment}")
					.addRequestHeader("X-Service-Name", "order-service"))
				.uri("lb://order-service"))

			// Store Service Swagger UI - Simple forwarding
			.route("store-service-swagger", r -> r
				.path("/store-service/**")
				.filters(f -> f
					.rewritePath("/store-service/(?<segment>.*)", "/${segment}")
					.addRequestHeader("X-Service-Name", "store-service"))
				.uri("lb://store-service"))

			// Payment Service Swagger UI - Simple forwarding
			.route("payment-service-swagger", r -> r
				.path("/payment-service/**")
				.filters(f -> f
					.rewritePath("/payment-service/(?<segment>.*)", "/${segment}")
					.addRequestHeader("X-Service-Name", "payment-service"))
				.uri("lb://payment-service"))

			.route("order-service", r -> r
				.path("/api/orders/**")
				.filters(f -> f
					.stripPrefix(1)
					.addRequestHeader("X-Service-Name", "order-service"))
				.uri("lb://order-service"))

			.route("store-service", r -> r
				.path("/api/stores/**")
				.filters(f -> f
					.stripPrefix(1)
					.addRequestHeader("X-Service-Name", "store-service"))
				.uri("lb://store-service"))

			.route("payment-service", r -> r
				.path("/api/payments/**")
				.filters(f -> f
					.stripPrefix(1)
					.addRequestHeader("X-Service-Name", "payment-service"))
				.uri("lb://payment-service"))

			.build();
	}
}