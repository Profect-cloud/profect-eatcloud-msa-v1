package com.eatcloud.adminservice.config;

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
                .url("/admin-service")
                .description("Admin Service via API Gateway");
                
        Server localServer = new Server()
                .url("/")
                .description("Direct Admin Service");
        
        return new OpenAPI()
                .servers(List.of(gatewayServer, localServer))
                .info(new Info()
                        .title("Admin Service API")
                        .version("1.0")
                        .description("Admin Service API Documentation"));
    }
}
