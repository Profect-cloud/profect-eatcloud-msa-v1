package com.eatcloud.storeservice.domain.store.ranking;

// src/main/java/com/eatcloud/storeservice/ranking/dto/RankTop3Response.java

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class RankTop3Response {

    private Integer storeCategoryId;
    private Integer rank;
    private UUID storeId;
    private String storeName;
    private BigDecimal avgRating;
    private Integer ratingCount;
    private OffsetDateTime updatedAt;
    private OffsetDateTime generatedAt;

    // All-args constructor
    public RankTop3Response(Integer storeCategoryId,
                            Integer rank,
                            UUID storeId,
                            String storeName,
                            BigDecimal avgRating,
                            Integer ratingCount,
                            OffsetDateTime updatedAt,
                            OffsetDateTime generatedAt) {
        this.storeCategoryId = storeCategoryId;
        this.rank = rank;
        this.storeId = storeId;
        this.storeName = storeName;
        this.avgRating = avgRating;
        this.ratingCount = ratingCount;
        this.updatedAt = updatedAt;
        this.generatedAt = generatedAt;
    }

    // Getters
    public Integer getStoreCategoryId() {
        return storeCategoryId;
    }

    public Integer getRank() {
        return rank;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public BigDecimal getAvgRating() {
        return avgRating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    // 정적 팩토리 메서드
    public static RankTop3Response from(RankTop3View v) {
        return new RankTop3Response(
                v.getStoreCategoryId(),
                v.getRank(),
                v.getStoreId(),
                v.getStoreName(),
                v.getAvgRating(),
                v.getRatingCount(),
                v.getUpdatedAt(),
                v.getGeneratedAt()
        );
    }
}

