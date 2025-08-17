package com.eatcloud.adminservice.config;

import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

// com.eatcloud.adminservice.config.ManagerAdminRestConfig
@Configuration
public class ManagerAdminRestConfig {
    @Bean
    RestClient managerAdminRestClient(@Value("${manager.admin.base-url}") String baseUrl) {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(5000);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(f).build();
    }
}
