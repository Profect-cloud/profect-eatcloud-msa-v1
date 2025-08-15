package com.eatcloud.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.eatcloud.orderservice.entity.OrderTypeCode;
import com.eatcloud.orderservice.common.BaseTimeRepository;

import java.util.Optional;

@Repository
public interface OrderTypeCodeRepository extends  BaseTimeRepository<OrderTypeCode, String> {
    Optional<OrderTypeCode> findByCode(String code);
}
