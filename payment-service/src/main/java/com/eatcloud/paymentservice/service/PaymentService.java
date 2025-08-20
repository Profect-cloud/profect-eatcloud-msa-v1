package com.eatcloud.paymentservice.service;

import com.eatcloud.paymentservice.entity.Payment;
import com.eatcloud.paymentservice.entity.PaymentMethodCode;
import com.eatcloud.paymentservice.entity.PaymentRequest;
import com.eatcloud.paymentservice.entity.PaymentStatus;
import com.eatcloud.paymentservice.entity.PaymentRequestStatus;
import com.eatcloud.paymentservice.event.PaymentCreatedEvent;
import com.eatcloud.paymentservice.repository.PaymentMethodCodeRepository;
import com.eatcloud.paymentservice.repository.PaymentRepository;
import com.eatcloud.paymentservice.repository.PaymentRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final TossPaymentService tossPaymentService;
    private final PaymentMethodCodeRepository paymentMethodCodeRepository;
    private final PaymentEventProducer paymentEventProducer;
    
    private static final long PAYMENT_TIMEOUT_MINUTES = 5;
    
    @Transactional
    public PaymentRequest createPaymentRequest(UUID orderId, UUID customerId, Integer amount) {
        log.info("결제 요청 생성: orderId={}, customerId={}, amount={}", orderId, customerId, amount);
        
        String redirectUrl = tossPaymentService.createPaymentRequest(
                orderId.toString(), amount, customerId.toString()
        );
        
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(orderId)
                .customerId(customerId)
                .pgProvider("TOSS")
                .requestPayload("{}") // 실제로는 더 상세한 정보를 저장
                .redirectUrl(redirectUrl)
                .status(PaymentRequestStatus.PENDING)
                .timeoutAt(LocalDateTime.now().plusMinutes(PAYMENT_TIMEOUT_MINUTES))
                .build();
        
        PaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);
        
        schedulePaymentTimeout(savedRequest.getPaymentRequestId());
        
        log.info("결제 요청 생성 완료: paymentRequestId={}, redirectUrl={}", 
                savedRequest.getPaymentRequestId(), redirectUrl);
        
        return savedRequest;
    }
    
    @Transactional
    public Payment confirmPayment(String paymentKey, String orderId, Integer amount) {
        log.info("결제 승인 처리: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
        
        Map<String, Object> tossResponse = tossPaymentService.confirmPayment(paymentKey, orderId, amount);
        
        PaymentRequest paymentRequest = paymentRequestRepository.findByOrderId(UUID.fromString(orderId))
                .orElseThrow(() -> new RuntimeException("결제 요청을 찾을 수 없습니다: " + orderId));
        
        Payment payment = Payment.builder()
                .orderId(UUID.fromString(orderId))
                .customerId(paymentRequest.getCustomerId())
                .totalAmount(amount)
                .pgTransactionId(paymentKey)
                .approvalCode(orderId)
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentMethod(mapTossMethodToPaymentMethod((String) tossResponse.get("method")))
                .approvedAt(LocalDateTime.now())
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        paymentRequest.updateStatus(PaymentRequestStatus.COMPLETED);
        paymentRequestRepository.save(paymentRequest);
        
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId(savedPayment.getPaymentId())
                .orderId(savedPayment.getOrderId())
                .customerId(savedPayment.getCustomerId())
                .totalAmount(savedPayment.getTotalAmount())
                .paymentStatus(savedPayment.getPaymentStatus().name())
                .paymentMethod(savedPayment.getPaymentMethod().getCode())
                .approvedAt(savedPayment.getApprovedAt())
                .build();
        
        paymentEventProducer.publishPaymentCreated(event);
        
        log.info("결제 승인 완료: paymentId={}, orderId={}", savedPayment.getPaymentId(), orderId);
        
        return savedPayment;
    }

    private PaymentMethodCode getMethodByCodeOrThrow(String code) {
        return paymentMethodCodeRepository.findById(code)
            .orElseThrow(() -> new IllegalStateException("결제수단 코드가 없습니다: " + code));
    }

    private PaymentMethodCode mapTossMethodToPaymentMethod(String tossMethod) {
        if (tossMethod == null) return getMethodByCodeOrThrow("CARD");

        String code = switch (tossMethod) {
            case "카드" -> "CARD";
            case "가상계좌" -> "VIRTUAL_ACCOUNT";
            case "계좌이체" -> "TRANSFER";
            case "휴대폰" -> "PHONE";
            case "상품권", "도서문화상품권", "게임문화상품권" -> "GIFT_CERTIFICATE";
            default -> "CARD";
        };
        return getMethodByCodeOrThrow(code);
    }
    
    @Async
    public CompletableFuture<Void> schedulePaymentTimeout(UUID paymentRequestId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 5분 대기
                Thread.sleep(PAYMENT_TIMEOUT_MINUTES * 60 * 1000);
                
                // 타임아웃 처리
                updateExpiredPaymentRequest(paymentRequestId);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("결제 타임아웃 작업이 중단되었습니다", e);
            } catch (Exception e) {
                log.error("결제 타임아웃 작업 중 오류 발생", e);
            }
        });
    }
    
    @Transactional
    public void updateExpiredPaymentRequest(UUID paymentRequestId) {
        paymentRequestRepository.findById(paymentRequestId)
                .ifPresent(paymentRequest -> {
                    if (PaymentRequestStatus.PENDING.equals(paymentRequest.getStatus())) {
                        paymentRequest.updateStatus(PaymentRequestStatus.TIMEOUT);
                        paymentRequest.setFailureReason("결제 타임아웃 - 5분 내 응답 없음");
                        paymentRequestRepository.save(paymentRequest);
                        
                        log.info("결제 요청 타임아웃 처리: paymentRequestId={}", paymentRequestId);
                    }
                });
    }
} 