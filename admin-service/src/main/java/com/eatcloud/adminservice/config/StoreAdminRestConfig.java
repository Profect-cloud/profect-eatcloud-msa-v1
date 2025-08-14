// config/StoreAdminRestConfig.java
package com.eatcloud.adminservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class StoreAdminRestConfig {

    @Bean
    public RestClient storeAdminRestClient(
            @Value("${store.admin.base-url}") String baseUrl
    ) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000); // 2s
        factory.setReadTimeout(5000);    // 5s

        return RestClient.builder()
                .baseUrl(baseUrl) // 예: http://localhost:8085 or http://store-service:8085
                .requestFactory(factory)
                .build();
    }
}

