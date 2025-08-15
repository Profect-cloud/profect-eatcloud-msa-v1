package com.eatcloud.orderservice.service;

import com.eatcloud.orderservice.entity.OrderStatus;
import com.eatcloud.orderservice.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {
    
    private final OrderService orderService;
    
    @KafkaListener(topics = "payment.created", groupId = "order-service")
    @Transactional
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("결제 생성 이벤트 수신: paymentId={}, orderId={}, status={}", 
                event.getPaymentId(), event.getOrderId(), event.getPaymentStatus());
        
        try {
            if ("COMPLETED".equals(event.getPaymentStatus())) {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);
                
                orderService.setPaymentId(event.getOrderId(), event.getPaymentId());
                
                log.info("주문 상태를 PAID로 업데이트 완료: orderId={}, paymentId={}", 
                        event.getOrderId(), event.getPaymentId());
                
            } else {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.FAILED);
                
                log.info("주문 상태를 FAILED로 업데이트 완료: orderId={}, paymentStatus={}", 
                        event.getOrderId(), event.getPaymentStatus());
            }
            
        } catch (Exception e) {
            log.error("결제 생성 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
            // 실제 운영에서는 Dead Letter Queue나 재시도 로직을 구현해야 합니다.
        }
    }
} 