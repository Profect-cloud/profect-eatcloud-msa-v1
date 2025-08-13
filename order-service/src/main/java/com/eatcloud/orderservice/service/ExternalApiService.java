package com.eatcloud.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.customer-service.url:http://customer-service}")
    private String customerServiceUrl;
    
    @Value("${services.store-service.url:http://store-service}")
    private String storeServiceUrl;
    
    /**
     * 고객 존재 여부 확인
     */
    public Boolean checkCustomerExists(UUID customerId) {
        try {
            String url = customerServiceUrl + "/customers/" + customerId + "/exists";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Boolean> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Boolean.class
            );
            
            log.info("Customer exists check successful for customerId: {}, result: {}", 
                customerId, response.getBody());
            return response.getBody() != null ? response.getBody() : false;
            
        } catch (RestClientException e) {
            log.error("Failed to check customer exists for customerId: {}", customerId, e);
            // Fallback: 기본값 반환 (실패 시 false)
            return false;
        }
    }
    
    /**
     * 고객 장바구니 무효화
     */
    public void invalidateCart(UUID customerId) {
        try {
            String url = customerServiceUrl + "/customers/" + customerId + "/cart/invalidate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Void.class
            );
            
            log.info("Cart invalidated successfully for customerId: {}", customerId);
            
        } catch (RestClientException e) {
            log.error("Failed to invalidate cart for customerId: {}", customerId, e);
            // 장바구니 무효화는 실패해도 주문 프로세스를 중단하지 않음
        }
    }
    
    /**
     * 메뉴 가격 조회
     */
    public Integer getMenuPrice(UUID menuId) {
        try {
            String url = storeServiceUrl + "/stores/menus/" + menuId + "/price";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Integer> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
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
            // 메뉴 가격 조회 실패는 주문을 중단해야 하므로 예외 발생
            throw new RuntimeException("Store service is temporarily unavailable for menu: " + menuId, e);
        }
    }
}
