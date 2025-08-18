// package com.eatcloud.customerservice.service;
//
// import com.eatcloud.customerservice.entity.Customer;
// import com.eatcloud.customerservice.entity.PointReservation;
// import com.eatcloud.customerservice.entity.ReservationStatus;
// import com.eatcloud.customerservice.event.PaymentCreatedEvent;
// import com.eatcloud.customerservice.repository.CustomerRepository;
// import com.eatcloud.customerservice.repository.PointReservationRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
//
// import java.util.Optional;
//
// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class PaymentEventConsumer {
//
//     private final CustomerRepository customerRepository;
//     private final PointReservationRepository pointReservationRepository;
//
//     @KafkaListener(topics = "payment.created", groupId = "customer-service")
//     @Transactional
//     public void handlePaymentCreated(PaymentCreatedEvent event) {
//         log.info("결제 생성 이벤트 수신: paymentId={}, orderId={}, customerId={}, status={}",
//                 event.getPaymentId(), event.getOrderId(), event.getCustomerId(), event.getPaymentStatus());
//
//         try {
//             if ("COMPLETED".equals(event.getPaymentStatus())) {
//                 processSuccessfulPayment(event);
//             } else {
//                 log.info("결제가 성공하지 않음: orderId={}, status={}", event.getOrderId(), event.getPaymentStatus());
//             }
//
//         } catch (Exception e) {
//             log.error("결제 생성 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
//             // 실제 운영에서는 Dead Letter Queue나 재시도 로직을 구현해야 합니다.
//         }
//     }
//
//     private void processSuccessfulPayment(PaymentCreatedEvent event) {
//         Optional<PointReservation> reservationOpt = pointReservationRepository.findByOrderId(event.getOrderId());
//
//         if (reservationOpt.isEmpty()) {
//             log.info("포인트 예약 정보가 없음: orderId={}", event.getOrderId());
//             return;
//         }
//
//         PointReservation reservation = reservationOpt.get();
//
//         if (ReservationStatus.PROCESSED.equals(reservation.getStatus()) ||
//             ReservationStatus.CANCELLED.equals(reservation.getStatus())) {
//             log.info("이미 처리된 포인트 예약: orderId={}, status={}", event.getOrderId(), reservation.getStatus());
//             return;
//         }
//
//         Customer customer = customerRepository.findById(event.getCustomerId());
//                 .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다: " + event.getCustomerId()));
//
//                     customer.deductReservedPoints(reservation.getPoints());
//             customerRepository.save(customer);
//
//             reservation.process();
//         reservation.process();
//         pointReservationRepository.save(reservation);
//
//         log.info("포인트 차감 완료: orderId={}, customerId={}, points={}, remainingPoints={}",
//                 event.getOrderId(), event.getCustomerId(), reservation.getPoints(), customer.getTotalPoints());
//     }
// }