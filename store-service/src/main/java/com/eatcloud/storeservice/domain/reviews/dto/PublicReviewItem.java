package com.eatcloud.storeservice.domain.reviews.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** 공개 응답 아이템 (p_reviews 컬럼과 1:1) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicReviewItem {
    private UUID reviewId;                // p_reviews.review_id
    private UUID orderId;                 // p_reviews.order_id
    private BigDecimal rating;            // p_reviews.rating (NUMERIC(2,1))
    private String content;               // p_reviews.content
    private LocalDateTime createdAt;      // p_reviews.created_at
    private String createdBy;             // p_reviews.created_by
    private LocalDateTime updatedAt;      // p_reviews.updated_at
    private String updatedBy;             // p_reviews.updated_by
}