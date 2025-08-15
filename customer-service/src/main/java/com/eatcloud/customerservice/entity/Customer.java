package com.eatcloud.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id")
    private UUID customerId;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;
    
    @Column(name = "reserved_points", nullable = false)
    private Integer reservedPoints;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (totalPoints == null) {
            totalPoints = 0;
        }
        if (reservedPoints == null) {
            reservedPoints = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void reservePoints(Integer points) {
        if (points > totalPoints) {
            throw new RuntimeException("보유 포인트가 부족합니다.");
        }
        this.reservedPoints += points;
    }
    
    public void deductReservedPoints(Integer points) {
        if (points > reservedPoints) {
            throw new RuntimeException("예약된 포인트가 부족합니다.");
        }
        this.reservedPoints -= points;
        this.totalPoints -= points;
    }
    
    public void releaseReservedPoints(Integer points) {
        if (points > reservedPoints) {
            throw new RuntimeException("예약된 포인트가 부족합니다.");
        }
        this.reservedPoints -= points;
    }
    
    public void addPoints(Integer points) {
        this.totalPoints += points;
    }
} 