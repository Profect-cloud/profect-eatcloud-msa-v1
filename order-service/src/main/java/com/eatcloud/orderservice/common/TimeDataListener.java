package com.eatcloud.orderservice.common;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;
import java.util.UUID;

public class TimeDataListener {

    @PrePersist
    public void prePersist(BaseTimeEntity entity) {
        if (entity.getTimeData() == null) {
            TimeData timeData = TimeData.builder()
                    .pTimeId(UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .createdBy(getCurrentUser())
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(getCurrentUser())
                    .build();
            entity.setTimeData(timeData);
        }
    }

    @PreUpdate
    public void preUpdate(BaseTimeEntity entity) {
        if (entity.getTimeData() != null) {
            entity.getTimeData().setUpdatedAt(LocalDateTime.now());
            entity.getTimeData().setUpdatedBy(getCurrentUser());
        }
    }

    private String getCurrentUser() {
        // MSA 환경에서는 헤더에서 사용자 정보를 가져오거나 기본값 사용
        // 추후 SecurityContext나 RequestContext에서 가져올 수 있음
        return "system";
    }
}
