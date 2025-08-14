package com.eatcloud.adminservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Bean
    @Qualifier("tossWebClient")
    public WebClient tossWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.tosspayments.com/v1")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }


    @Bean(name = "geminiWebClient")
    public WebClient geminiWebClient(@Value("${google.ai.api.base-url}") String googleAiBaseUrl) {
        return WebClient.builder()
                .baseUrl(googleAiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
