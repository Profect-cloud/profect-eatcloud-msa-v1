package com.eatcloud.customerservice.service;

import com.eatcloud.customerservice.event.PaymentCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PointReservationService pointReservationService;
    private final ObjectMapper objectMapper;
    private final com.eatcloud.customerservice.circuit.CircuitBreaker circuitBreaker;

    @KafkaListener(topics = "payment.created", groupId = "customer-service", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handlePaymentCreated(String eventJson) {
        // Circuit Breaker 상태 확인
        if (!circuitBreaker.canExecute()) {
            log.warn("Circuit Breaker가 OPEN 상태입니다. 메시지 처리를 건너뜁니다: {}", eventJson);
            return;
        }

        try {
            log.info("결제 생성 이벤트 수신 (JSON): {}", eventJson);
            
            // JSON 문자열을 PaymentCreatedEvent로 파싱
            PaymentCreatedEvent event = objectMapper.readValue(eventJson, PaymentCreatedEvent.class);
            
            log.info("결제 생성 이벤트 파싱 완료: paymentId={}, orderId={}, customerId={}, status={}",
                    event.getPaymentId(), event.getOrderId(), event.getCustomerId(), event.getPaymentStatus());

            if ("COMPLETED".equals(event.getPaymentStatus())) {
                // 결제 완료 시 포인트 예약 처리
                pointReservationService.processReservation(event.getOrderId());
                log.info("결제 완료로 인한 포인트 예약 처리 완료: orderId={}, paymentId={}", 
                        event.getOrderId(), event.getPaymentId());
            } else if ("FAILED".equals(event.getPaymentStatus()) || "CANCELLED".equals(event.getPaymentStatus())) {
                // 결제 실패/취소 시 포인트 예약 취소 (환불)
                pointReservationService.cancelReservation(event.getOrderId());
                log.info("결제 실패/취소로 인한 포인트 예약 취소 완료: orderId={}, paymentId={}, status={}", 
                        event.getOrderId(), event.getPaymentId(), event.getPaymentStatus());
            } else {
                log.info("결제가 성공하지 않음: orderId={}, status={}", event.getOrderId(), event.getPaymentStatus());
            }

            // 성공 시 Circuit Breaker 상태 업데이트
            circuitBreaker.onSuccess();

        } catch (JsonProcessingException e) {
            log.error("결제 생성 이벤트 JSON 파싱 실패: eventJson={}", eventJson, e);
            // JSON 파싱 실패는 트랜잭션 롤백하지 않음
            circuitBreaker.onFailure(e);
        } catch (Exception e) {
            log.error("결제 생성 이벤트 처리 실패: eventJson={}", eventJson, e);
            // Circuit Breaker 실패 상태 업데이트
            circuitBreaker.onFailure(e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }
}