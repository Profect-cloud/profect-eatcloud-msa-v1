package com.eatcloud.paymentservice.repository;

import com.eatcloud.paymentservice.entity.PaymentMethodCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodCodeRepository extends JpaRepository<PaymentMethodCode, String> {
}