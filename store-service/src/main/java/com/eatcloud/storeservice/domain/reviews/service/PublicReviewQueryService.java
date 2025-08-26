package com.eatcloud.storeservice.domain.reviews.service;

import com.eatcloud.storeservice.domain.reviews.dto.*;
import com.eatcloud.storeservice.domain.reviews.util.SortWhitelist;
import com.eatcloud.storeservice.reviews.client.OrdersReviewClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicReviewQueryService {

    private final OrdersReviewClient orders;

    /**
     * 지정된 매장의 공개 리뷰 목록과 평점 요약을 함께 조회하여 반환합니다.
     *
     * <p>필터의 minRating, hasImage, from, to 값을 사용해 리뷰를 조회하고,
     * 페이징 결과의 내용을 PublicReviewItem으로 매핑한 뒤 페이지 페이로드와 평점 요약을 조합해 응답을 생성합니다.</p>
     *
     * @param storeId 조회 대상 매장의 UUID
     * @param filter  조회에 사용되는 필터 (minRating, hasImage, from, to를 사용)
     * @param pageable 페이징/정렬 정보 — 정렬은 오직 `createdAt`과 `rating` 필드만 허용됩니다
     * @return 매장의 평점 요약과 페이징된 공개 리뷰 목록을 담은 PublicReviewListResponse
     */
    @Transactional(readOnly = true)
    public PublicReviewListResponse list(UUID storeId, PublicReviewFilter filter, Pageable pageable) {
        // createdAt, rating 만 정렬 허용
        pageable = SortWhitelist.enforce(pageable, Set.of("createdAt", "rating"));

        OrdersReviewPageResponse page = orders.fetchReviews(
                storeId,
                filter.getMinRating(),
                filter.getHasImage(),
                filter.getFrom(),
                filter.getTo(),
                pageable
        );

        RatingSummaryResponse summary = orders.fetchSummary(storeId);

        var items = page.getContent().stream().map(r ->
                PublicReviewItem.builder()
                        .reviewId(r.getReviewId())
                        .orderId(r.getOrderId())
                        .rating(r.getRating())
                        .content(r.getContent())
                        .createdAt(r.getCreatedAt())
                        .createdBy(r.getCreatedBy())
                        .updatedAt(r.getUpdatedAt())
                        .updatedBy(r.getUpdatedBy())
                        .build()
        ).collect(Collectors.toList());

        PublicReviewListResponse.PagePayload<PublicReviewItem> payload =
                PublicReviewListResponse.PagePayload.<PublicReviewItem>builder()
                        .content(items)
                        .page(page.getPage())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return PublicReviewListResponse.builder()
                .storeId(storeId)
                .ratingSummary(summary)
                .reviews(payload)
                .build();
    }

    /**
     * 지정한 매장의 평점 요약을 조회합니다.
     *
     * OrdersReviewClient를 통해 storeId에 대한 RatingSummaryResponse를 가져와 반환합니다.
     *
     * @param storeId 조회할 매장의 UUID
     * @return 해당 매장의 평점 요약 정보
     */
    @Transactional(readOnly = true)
    public RatingSummaryResponse summary(UUID storeId) {
        return orders.fetchSummary(storeId);
    }
}
