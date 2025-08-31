package com.eatcloud.managerservice.config;

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
                .url("/manager-service")
                .description("Manager Service via API Gateway");
                
        Server localServer = new Server()
                .url("/")
                .description("Direct Manager Service");
        
        return new OpenAPI()
                .servers(List.of(gatewayServer, localServer))
                .info(new Info()
                        .title("Manager Service API")
                        .version("1.0")
                        .description("Manager Service API Documentation"));
    }
}
