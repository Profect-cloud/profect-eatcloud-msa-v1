package com.eatcloud.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {
    
    private final WebClient tossWebClient;
    
    @Value("${toss.secret-key}")
    private String secretKey;
    
    public Map<String, Object> confirmPayment(String paymentKey, String orderId, Integer amount) {
        log.info("토스페이먼츠 결제 승인 요청: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
        
        String encodedAuth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());
        
        Map<String, Object> request = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );
        
        try {
            Map<String, Object> response = tossWebClient
                    .post()
                    .uri("/payments/confirm")
                    .header("Authorization", "Basic " + encodedAuth)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            log.info("토스페이먼츠 결제 승인 성공: orderId={}", orderId);
            return response;
            
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 실패: orderId={}", orderId, e);
            throw new RuntimeException("결제 승인 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    public String createPaymentRequest(String orderId, Integer amount, String customerId) {
        log.info("토스페이먼츠 결제 요청 생성: orderId={}, amount={}, customerId={}", orderId, amount, customerId);
        
        // 실제로는 토스페이먼츠 SDK의 payment.requestPayment() 함수를 호출해야 합니다.
        // 여기서는 예시로 결제 요청 URL을 반환합니다.
        String redirectUrl = String.format(
                "https://pay.toss.im/?orderId=%s&amount=%d&customerId=%s",
                orderId, amount, customerId
        );
        
        log.info("토스페이먼츠 결제 요청 URL 생성: {}", redirectUrl);
        return redirectUrl;
    }
} 