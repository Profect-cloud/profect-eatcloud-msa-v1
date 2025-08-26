package com.eatcloud.storeservice.domain.reviews.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** orders-service에서 p_reviews를 기반으로 내려주는 항목 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdersReviewItem {
    private UUID reviewId;
    private UUID orderId;
    private BigDecimal rating;
    private String content;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
