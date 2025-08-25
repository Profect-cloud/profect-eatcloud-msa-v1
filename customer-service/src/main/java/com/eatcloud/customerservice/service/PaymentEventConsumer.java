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

    @KafkaListener(topics = "payment.created", groupId = "customer-service", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handlePaymentCreated(String eventJson) {
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
            } else {
                log.info("결제가 성공하지 않음: orderId={}, status={}", event.getOrderId(), event.getPaymentStatus());
            }

        } catch (JsonProcessingException e) {
            log.error("결제 생성 이벤트 JSON 파싱 실패: eventJson={}", eventJson, e);
            // JSON 파싱 실패는 트랜잭션 롤백하지 않음
        } catch (Exception e) {
            log.error("결제 생성 이벤트 처리 실패: eventJson={}", eventJson, e);
            // TODO: Dead Letter Queue 구현 필요
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }
}