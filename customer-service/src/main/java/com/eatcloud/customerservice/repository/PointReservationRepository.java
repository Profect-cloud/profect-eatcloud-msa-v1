package com.eatcloud.customerservice.repository;

import com.eatcloud.autotime.repository.SoftDeleteRepository;
import com.eatcloud.customerservice.entity.PointReservation;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PointReservationRepository extends SoftDeleteRepository<PointReservation, UUID> {
    Optional<PointReservation> findByOrderId(UUID orderId);
} 