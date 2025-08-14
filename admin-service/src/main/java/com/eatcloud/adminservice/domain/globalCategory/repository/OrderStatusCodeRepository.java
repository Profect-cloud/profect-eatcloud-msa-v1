package com.eatcloud.adminservice.domain.globalCategory.repository;

import org.springframework.stereotype.Repository;
import profect.eatcloud.domain.globalCategory.entity.OrderStatusCode;
import profect.eatcloud.global.timeData.BaseTimeRepository;

import java.util.Optional;

@Repository
public interface OrderStatusCodeRepository extends BaseTimeRepository<OrderStatusCode, String> {
    Optional<OrderStatusCode> findByCode(String code);
}