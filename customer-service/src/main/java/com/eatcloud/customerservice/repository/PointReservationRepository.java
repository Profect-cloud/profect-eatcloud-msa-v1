package com.eatcloud.customerservice.repository;

import com.eatcloud.customerservice.entity.PointReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PointReservationRepository extends JpaRepository<PointReservation, UUID> {
    Optional<PointReservation> findByOrderId(UUID orderId);
} 