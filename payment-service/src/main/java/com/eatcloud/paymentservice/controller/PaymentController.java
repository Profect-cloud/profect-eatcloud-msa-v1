package com.eatcloud.paymentservice.controller;

import com.eatcloud.paymentservice.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Service", description = "결제 서비스 API")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/confirm")
    @Operation(summary = "결제 승인", description = "토스페이먼츠 결제 승인을 처리합니다.")
    public ResponseEntity<String> confirmPayment(@RequestBody Map<String, Object> request) {
        try {
            String paymentKey = (String) request.get("paymentKey");
            String orderId = (String) request.get("orderId");
            Integer amount = (Integer) request.get("amount");
            
            log.info("결제 승인 요청: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
            
            paymentService.confirmPayment(paymentKey, orderId, amount);
            
            return ResponseEntity.ok("결제가 성공적으로 처리되었습니다.");
            
        } catch (Exception e) {
            log.error("결제 승인 실패", e);
            return ResponseEntity.badRequest().body("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @GetMapping("/status/{orderId}")
    @Operation(summary = "결제 상태 확인", description = "주문 ID로 결제 상태를 조회합니다.")
    public ResponseEntity<String> getPaymentStatus(@PathVariable String orderId) {
        log.info("결제 상태 확인 요청: orderId={}", orderId);
        return ResponseEntity.ok("결제 상태 확인 API - 구현 예정");
    }
} 