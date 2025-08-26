package com.eatcloud.storeservice.domain.reviews.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RatingSummaryResponse {
    private BigDecimal avgRating;          // 평균 별점
    private int ratingCount;               // 리뷰 수
    private BigDecimal rating30dAvg;       // 최근 30일 평균
    private Map<Integer, Integer> histogram; // 1~5 별 카운트
}
