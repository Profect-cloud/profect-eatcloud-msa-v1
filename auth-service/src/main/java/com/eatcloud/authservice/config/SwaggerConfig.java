package com.eatcloud.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        Server gatewayServer = new Server()
                .url("/auth-service")
                .description("Auth Service via API Gateway");
                
        Server localServer = new Server()
                .url("/")
                .description("Direct Auth Service");
        
        return new OpenAPI()
                .servers(List.of(gatewayServer, localServer))
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0")
                        .description("Auth Service API Documentation"));
    }
}
