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

    /**
     * 클라이언트 사이드 로드밸런싱이 적용된 RestClient.Builder 빈을 제공합니다.
     *
     * 다른 구성 빈에서 주입받아 서비스 ID 기반 호출(예: `lb://{serviceId}`)을 사용하도록 baseUrl을 설정하고
     * RestClient 인스턴스를 생성하는 데 사용됩니다.
     *
     * @return 로드밸런싱이 활성화된 RestClient.Builder 인스턴스
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Admin 서비스에 대한 RestClient 빈을 생성한다.
     *
     * 지정된 서비스 ID를 사용해 "lb://{serviceId}" 형식의 로드밸런스된 기본 URL을 설정한 RestClient를 반환한다.
     *
     * @param serviceId Admin 서비스의 서비스 ID (프로퍼티 {@code admin.service-id}, 기본값 {@code admin-service})
     * @return 로드밸런싱(Eureka 등)된 기본 URL로 구성된 RestClient 인스턴스
     */
    @Bean(name = "adminRestClient")
    public RestClient adminRestClient(
            @Value("${admin.service-id:admin-service}") String serviceId,
            RestClient.Builder builder
    ) {
        return builder
                .baseUrl("lb://" + serviceId)   // Eureka 서비스ID 기반 호출
                .build();
    }

    /**
     * Orders 서비스와의 통신에 사용되는 RestClient 빈을 생성한다.
     *
     * 지정된 서비스 ID를 이용해 로드밸런싱된 base URL("lb://{serviceId}")을 설정한 RestClient 인스턴스를 반환한다.
     *
     * @param serviceId Spring 프로퍼티 `orders.service-id`(기본값: "orders-service") — 호출 대상 Orders 서비스의 서비스 ID
     * @return 로드밸런싱된 서비스 식별자(lb://{serviceId})를 기본 URL로 사용하는 구성된 RestClient
     */
    @Bean(name = "ordersRestClient")
    public RestClient ordersRestClient(
            @Value("${orders.service-id:orders-service}") String serviceId,
            RestClient.Builder builder
    ) {
        return builder
                .baseUrl("lb://" + serviceId)   // Eureka 서비스ID 기반 호출
                .build();
    }
}
