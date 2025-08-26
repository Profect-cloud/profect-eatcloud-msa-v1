package com.eatcloud.storeservice.domain.reviews.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicReviewFilter {
    private Integer minRating;          // 예: 4 이상
    private Boolean hasImage;           // orders가 쓰지 않으면 무시됨
    private LocalDateTime from;
    private LocalDateTime to;
}
