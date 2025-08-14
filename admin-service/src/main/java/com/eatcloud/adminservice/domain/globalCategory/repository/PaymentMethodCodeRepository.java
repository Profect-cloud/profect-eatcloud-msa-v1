package com.eatcloud.adminservice.domain.globalCategory.repository;

import org.springframework.stereotype.Repository;
import profect.eatcloud.domain.globalCategory.entity.PaymentMethodCode;
import profect.eatcloud.global.timeData.BaseTimeRepository;

import java.util.Optional;

@Repository
public interface PaymentMethodCodeRepository extends BaseTimeRepository<PaymentMethodCode, String> {
    Optional<PaymentMethodCode> findByCode(String code);
}