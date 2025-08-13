package com.eatcloud.orderservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    private UUID storeId;
    private String orderType; // DELIVERY or PICKUP
    private Boolean usePoints;
    private Integer pointsToUse;
    
    // Delivery 주문인 경우
    private String deliveryAddress;
    private String deliveryRequests;
    
    // Pickup 주문인 경우
    private String pickupRequests;
}
