// com.eatcloud.adminservice.config.RemoteClientsConfig
package com.eatcloud.adminservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RemoteClientsConfig {

    private SimpleClientHttpRequestFactory factory() {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(5000);
        return f;
    }

    @Bean
    public RestClient customerAdminRestClient(@Value("${customer.admin.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory()).build();
    }

    @Bean
    public RestClient managerDirectoryRestClient(@Value("${manager.admin.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory()).build();
    }

    @Bean
    public RestClient storeDirectoryRestClient(@Value("${store.admin.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory()).build();
    }
}
