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

    // 이름을 명시적으로 지정
    @Bean(name = "adminRestClient")
    public RestClient adminRestClient(
            @Value("${admin.base-url}") String baseUrl,
            RestClient.Builder builder
    ) {
        return builder
                .baseUrl(baseUrl)   // eureka(유레카) 사용할 땐: http://admin-service
                .build();
    }
}
