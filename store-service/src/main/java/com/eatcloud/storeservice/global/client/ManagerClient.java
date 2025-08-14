package com.eatcloud.storeservice.global.client;

import com.eatcloud.storeservice.global.client.dto.ManagerInfoResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class ManagerClient {

    private final RestTemplate restTemplate;

    // 기본값(로컬/도커 네트워크). 필요하면 application.yml에서 override 가능
    private final String baseUrl;

    public ManagerClient(RestTemplate restTemplate,
                         org.springframework.core.env.Environment env) {
        this.restTemplate = restTemplate;
        this.baseUrl = env.getProperty("user.service.url", "http://user-service:8081");
    }

    public ManagerInfoResponse getManagerById(UUID id) {
        try {
            String url = baseUrl + "/api/v1/managers/" + id;
            return restTemplate.getForObject(url, ManagerInfoResponse.class);
        } catch (RestClientException e) {
            // 필요시 커스텀 예외로 래핑
            throw new IllegalStateException("Failed to call user-service: " + e.getMessage(), e);
        }
    }
}
