// store-service / config/RestClientsConfig.java
package com.eatcloud.storeservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientsConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // Admin 서비스 호출용
    @Bean(name = "adminRestClient")
    @Qualifier("adminRestClient")
    public RestClient adminRestClient(
            @Value("${admin.service-id:admin-service}") String serviceId,
            RestClient.Builder builder
    ) {
        return builder
                .baseUrl("lb://" + serviceId)   // Eureka 서비스ID 기반 호출
                .build();
    }

    // Orders 서비스 호출용
    @Bean(name = "ordersRestClient")
    @Qualifier("ordersRestClient")
    public RestClient ordersRestClient(
            @Value("${orders.service-id:orders-service}") String serviceId,
            RestClient.Builder builder
    ) {
        return builder
                .baseUrl("lb://" + serviceId)   // Eureka 서비스ID 기반 호출
                .build();
    }
}
