package com.eatcloud.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "point_reservations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointReservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reservation_id")
    private UUID reservationId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @Column(name = "points", nullable = false)
    private Integer points;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;
    
    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        reservedAt = LocalDateTime.now();
        if (status == null) {
            status = ReservationStatus.RESERVED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void process() {
        this.status = ReservationStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }
} 