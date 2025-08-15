package com.eatcloud.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.eatcloud.orderservice.entity.OrderStatusCode;
import com.eatcloud.orderservice.common.BaseTimeRepository;

import java.util.Optional;

@Repository
public interface OrderStatusCodeRepository extends  BaseTimeRepository<OrderStatusCode, String> {
    Optional<OrderStatusCode> findByCode(String code);
}
