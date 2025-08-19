// admin-service
package com.eatcloud.adminservice.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StoreInternalPingClient {
    private final RestClient storeInternalClient;

    public Map<String, Object> ping() {
        return storeInternalClient.get()
                .uri("/internal/ping")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
