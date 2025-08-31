package com.eatcloud.orderservice.config;

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
                .url("/order-service")
                .description("Order Service via API Gateway");
                
        Server localServer = new Server()
                .url("/")
                .description("Direct Order Service");
        
        return new OpenAPI()
                .servers(List.of(gatewayServer, localServer))
                .info(new Info()
                        .title("Order Service API")
                        .version("1.0")
                        .description("Order Service API Documentation"));
    }
}
