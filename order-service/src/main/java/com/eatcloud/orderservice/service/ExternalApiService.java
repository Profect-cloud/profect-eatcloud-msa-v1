package com.eatcloud.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.UUID;

@Slf4j
@Service
public class ExternalApiService {

    private final RestTemplate restTemplate;

    // @LoadBalanced가 적용된 RestTemplate 주입
    public ExternalApiService(@Qualifier("restTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 고객 존재 여부 확인
     */
    public Boolean checkCustomerExists(UUID customerId) {
        try {
            String url = "http://customer-service/customers/" + customerId + "/exists";

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Boolean.class
            );

            log.info("Customer exists check successful for customerId: {}, result: {}",
                    customerId, response.getBody());
            return response.getBody() != null ? response.getBody() : false;

        } catch (RestClientException e) {
            log.error("Failed to check customer exists for customerId: {}", customerId, e);
            // Fallback: 서비스 장애 시에도 주문 진행 가능하도록
            return true;
        }
    }



    /**
     * 메뉴 가격 조회
     */
    public Integer getMenuPrice(UUID menuId) {
        try {
            String url = "http://store-service/stores/menus/" + menuId + "/price";

            ResponseEntity<Integer> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Integer.class
            );

            Integer price = response.getBody();
            log.info("Menu price retrieved successfully for menuId: {}, price: {}", menuId, price);

            if (price == null) {
                throw new RuntimeException("Menu price is null for menuId: " + menuId);
            }

            return price;

        } catch (RestClientException e) {
            log.error("Failed to get menu price for menuId: {}", menuId, e);
            throw new RuntimeException("Store service is temporarily unavailable for menu: " + menuId, e);
        }
    }
}