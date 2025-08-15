package com.eatcloud.paymentservice.repository;

import com.eatcloud.paymentservice.entity.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    Optional<PaymentRequest> findByOrderId(UUID orderId);
    List<PaymentRequest> findByStatusAndTimeoutAtBefore(String status, LocalDateTime timeoutAt);
} 