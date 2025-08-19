// admin-service
package com.eatcloud.adminservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class InternalClientsConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder lbRestClientBuilder() {
        return RestClient.builder();
    }

    // Eureka 서비스명으로 직접 호출 (게이트웨이 경유 아님)
    @Bean
    public RestClient storeInternalClient(RestClient.Builder lbBuilder) {
        return lbBuilder.baseUrl("http://store-service").build();
    }
}
