// store-service / config/RestClientsConfig.java
package com.eatcloud.storeservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientsConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @Qualifier("adminRestClient")
    public RestClient adminRestClient(
            @Value("${clients.admin.base-url:lb://admin-service}") String baseUrl,
            RestClient.Builder builder
    ) {
        // baseUrl 은 eureka 서비스명 사용
        return builder.baseUrl(baseUrl).build();
    }
}
