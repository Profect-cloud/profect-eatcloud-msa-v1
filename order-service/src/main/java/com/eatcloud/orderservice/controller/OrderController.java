package com.eatcloud.orderservice.controller;

import com.eatcloud.orderservice.dto.CreateOrderRequest;
import com.eatcloud.orderservice.dto.CreateOrderResponse;
import com.eatcloud.orderservice.entity.OrderStatus;
import com.eatcloud.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Service", description = "주문 서비스 API")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("주문 생성 요청: customerId={}, storeId={}", request.getCustomerId(), request.getStoreId());
        
        try {
            CreateOrderResponse response = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("주문 생성 실패: customerId={}, storeId={}", request.getCustomerId(), request.getStoreId(), e);
            throw e;
        }
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 조회", description = "주문 ID로 주문 정보를 조회합니다.")
    public ResponseEntity<String> getOrder(@PathVariable UUID orderId) {
        log.info("주문 조회 요청: orderId={}", orderId);
        return ResponseEntity.ok("주문 조회 API - 구현 예정");
    }
    
    @PatchMapping("/{orderId}/status")
    @Operation(summary = "주문 상태 업데이트", description = "주문의 상태를 업데이트합니다.")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {
        log.info("주문 상태 업데이트 요청: orderId={}, status={}", orderId, status);
        
        try {
            orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok("주문 상태가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("주문 상태 업데이트 실패: orderId={}, status={}", orderId, status, e);
            throw e;
        }
    }
    
    @PatchMapping("/{orderId}/payment")
    @Operation(summary = "주문에 결제 ID 설정", description = "주문에 결제 ID를 설정합니다.")
    public ResponseEntity<String> setPaymentId(
            @PathVariable UUID orderId,
            @RequestParam UUID paymentId) {
        log.info("주문에 결제 ID 설정 요청: orderId={}, paymentId={}", orderId, paymentId);
        
        try {
            orderService.setPaymentId(orderId, paymentId);
            return ResponseEntity.ok("주문에 결제 ID가 성공적으로 설정되었습니다.");
        } catch (Exception e) {
            log.error("주문에 결제 ID 설정 실패: orderId={}, paymentId={}", orderId, paymentId, e);
            throw e;
        }
    }
} 