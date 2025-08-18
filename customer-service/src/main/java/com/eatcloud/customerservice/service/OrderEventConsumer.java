package com.eatcloud.customerservice.service;

import com.eatcloud.customerservice.entity.Customer;
import com.eatcloud.customerservice.entity.PointReservation;
import com.eatcloud.customerservice.entity.ReservationStatus;
import com.eatcloud.customerservice.event.OrderCreatedEvent;
import com.eatcloud.customerservice.repository.CustomerRepository;
import com.eatcloud.customerservice.repository.PointReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {
    
    private final CustomerRepository customerRepository;
    private final PointReservationRepository pointReservationRepository;
    
    // @KafkaListener(topics = "order.created", groupId = "customer-service")
    // @Transactional
    // public void handleOrderCreated(OrderCreatedEvent event) {
    //     log.info("주문 생성 이벤트 수신: orderId={}, customerId={}, pointsToUse={}",
    //             event.getOrderId(), event.getCustomerId(), event.getPointsToUse());
    //
    //     try {
    //         if (event.getPointsToUse() == null || event.getPointsToUse() <= 0) {
    //             log.info("포인트 사용 없음: orderId={}", event.getOrderId());
    //             return;
    //         }
    //
    //         Customer customer = customerRepository.findById(event.getCustomerId());
    //                 .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다: " + event.getCustomerId()));
    //
    //         customer.reservePoints(event.getPointsToUse());
    //         customerRepository.save(customer);
    //
    //         PointReservation reservation = PointReservation.builder()
    //                 .customerId(event.getCustomerId())
    //                 .orderId(event.getOrderId())
    //                 .points(event.getPointsToUse())
    //                 .status(ReservationStatus.RESERVED)
    //                 .build();
    //
    //         pointReservationRepository.save(reservation);
    //
    //         log.info("포인트 차감 예약 완료: orderId={}, customerId={}, points={}",
    //                 event.getOrderId(), event.getCustomerId(), event.getPointsToUse());
    //
    //     } catch (Exception e) {
    //         log.error("주문 생성 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
    //         // 실제 운영에서는 Dead Letter Queue나 재시도 로직을 구현해야 합니다.
    //     }
    // }
} 