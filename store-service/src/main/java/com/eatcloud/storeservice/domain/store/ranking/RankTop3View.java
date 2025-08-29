package com.eatcloud.storeservice.domain.store.ranking;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;

public interface RankTop3View {
    Integer getStoreCategoryId();
    Integer getRank();
    UUID getStoreId();
    String getStoreName();
    BigDecimal getAvgRating();
    Integer getRatingCount();
    LocalDateTime getUpdatedAt();     // ★ OffsetDateTime -> LocalDateTime
    LocalDateTime getGeneratedAt();   // ★ OffsetDateTime -> LocalDateTime
}
