package com.eatcloud.storeservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfig {
    //http://localhost:8080/swagger-ui/index.html
    // https://fiteam.shop/swagger-ui/index.html
    @Bean
    public OpenAPI customOpenAPI() {
        // 보안 스킴 정의
        Components components = new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name(HttpHeaders.AUTHORIZATION)
                );

        // 전역 보안 요구 추가
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .components(components)
                .addSecurityItem(securityRequirement)
                .info(info());
    }

    private Info info() {
        return new Info()
                .title("Fiteam API Documentation")
                .description("")
                .version("1.1");
    }
}
