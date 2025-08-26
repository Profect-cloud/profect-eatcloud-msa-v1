package com.eatcloud.storeservice.domain.reviews.controller;

import com.eatcloud.storeservice.domain.reviews.dto.PublicReviewFilter;
import com.eatcloud.storeservice.domain.reviews.dto.PublicReviewListResponse;
import com.eatcloud.storeservice.domain.reviews.dto.RatingSummaryResponse;
import com.eatcloud.storeservice.domain.reviews.service.PublicReviewQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class PublicReviewController {

    private final PublicReviewQueryService service;

    /**
     * 지정한 가게의 공개 리뷰 목록을 조회한다.
     *
     * <p>지원하는 쿼리 파라미터로 필터링 및 페이징/정렬을 수행한다.</p>
     *
     * @param storeId 조회할 가게의 UUID
     * @param minRating 최소 평점으로 필터링(미지정 시 제한 없음)
     * @param hasImage 이미지가 포함된 리뷰만 조회하려면 true, 미지정 시 전체
     * @param from 조회 시작일시(ISO-8601 형식, 예: yyyy-MM-dd'T'HH:mm:ss)
     * @param to 조회 종료일시(ISO-8601 형식)
     * @param page 페이지 번호(0부터 시작, 기본 0)
     * @param size 페이지 크기(기본 20)
     * @param sort 정렬 문자열(형식: "property[,asc|desc]"; 기본 "createdAt,desc", 방향이 "asc"인 경우 오름차순, 그 외는 내림차순)
     * @return 요청 조건에 따른 PublicReviewListResponse(페이징된 리뷰 목록)
     */
    @GetMapping("/{storeId}/reviews")
    public PublicReviewListResponse list(
            @PathVariable UUID storeId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        // sort 파싱 (예: createdAt,desc)
        String[] sp = sort.split(",", 2);
        String prop = sp[0];
        Sort.Direction dir = (sp.length > 1 && "asc".equalsIgnoreCase(sp[1]))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(dir, prop)));

        PublicReviewFilter filter = PublicReviewFilter.builder()
                .minRating(minRating)
                .hasImage(hasImage)
                .from(from)
                .to(to)
                .build();

        return service.list(storeId, filter, pageable);
    }

    /**
     * 지정한 가게의 평점 요약 정보를 조회하여 반환합니다.
     *
     * 요청한 가게의 전체 평점 분포와 요약 통계(예: 평균, 총평점 수 등)를 포함한
     * RatingSummaryResponse를 반환합니다.
     *
     * @param storeId 조회할 가게의 UUID
     * @return 해당 가게의 평점 요약을 담은 RatingSummaryResponse
     */
    @GetMapping("/{storeId}/ratings/summary")
    public RatingSummaryResponse summary(@PathVariable UUID storeId) {
        return service.summary(storeId);
    }
}
