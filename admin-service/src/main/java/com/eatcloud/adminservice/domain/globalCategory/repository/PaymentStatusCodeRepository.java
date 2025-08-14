package com.eatcloud.adminservice.domain.globalCategory.repository;

import org.springframework.stereotype.Repository;
import profect.eatcloud.domain.globalCategory.entity.PaymentStatusCode;
import profect.eatcloud.global.timeData.BaseTimeRepository;

import java.util.Optional;

@Repository
public interface PaymentStatusCodeRepository extends BaseTimeRepository<PaymentStatusCode, String> {
    Optional<PaymentStatusCode> findByCode(String code);
}