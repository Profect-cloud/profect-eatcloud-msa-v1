package com.eatcloud.storeservice.reviews.client;

import com.eatcloud.storeservice.domain.reviews.dto.OrdersReviewPageResponse;
import com.eatcloud.storeservice.domain.reviews.dto.RatingSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdersReviewClient {

    private final RestClient ordersRestClient;

    /**
     * 지정한 조건으로 주문 리뷰 페이지를 조회한다.
     *
     * 선택된 정렬(페이지 요청의 첫 번째 정렬 기준)이 있으면 해당 정렬을 사용하고, 없으면 기본값 "createdAt,desc"를 사용하여
     * 내부 엔드포인트 "/internal/v1/reviews"에 GET 요청을 보낸 뒤 결과를 OrdersReviewPageResponse로 반환한다.
     *
     * Optional 파라미터(minRating, hasImage, from, to)는 null일 경우 쿼리에서 제외된다. pageable의 페이지 번호와 크기, 계산된 sort 파라미터가 쿼리에 포함되며,
     * URI는 인코딩(build(true))된 문자열로 생성된다.
     *
     * @param storeId 조회할 가맹점의 UUID
     * @param minRating 최소 평점(없으면 null로 전달하여 필터링하지 않음)
     * @param hasImage 이미지 포함 여부(없으면 null로 전달하여 필터링하지 않음)
     * @param from 조회 시작 시점(없으면 null)
     * @param to 조회 종료 시점(없으면 null)
     * @param pageable 페이징 및 정렬 정보(첫 번째 정렬 기준을 사용하여 sort 파라미터를 구성)
     * @return 요청 조건에 해당하는 OrdersReviewPageResponse
     */
    public OrdersReviewPageResponse fetchReviews(
            UUID storeId, Integer minRating, Boolean hasImage,
            LocalDateTime from, LocalDateTime to, org.springframework.data.domain.Pageable pageable
    ) {
        String sortParam = pageable.getSort().stream().findFirst()
                .map(o -> o.getProperty() + "," + o.getDirection().name().toLowerCase())
                .orElse("createdAt,desc");

        String uri = UriComponentsBuilder.fromPath("/internal/v1/reviews")
                .queryParam("storeId", storeId)
                .queryParamIfPresent("minRating", Optional.ofNullable(minRating))
                .queryParamIfPresent("hasImage", Optional.ofNullable(hasImage))
                .queryParamIfPresent("from", Optional.ofNullable(from))
                .queryParamIfPresent("to", Optional.ofNullable(to))
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("sort", sortParam)
                .build(true).toUriString();

        return ordersRestClient.get()
                .uri(uri)
                .retrieve()
                .body(OrdersReviewPageResponse.class);
    }

    /**
     * 지정한 매장의 리뷰 평점 요약(RatingSummaryResponse)을 조회합니다.
     *
     * <p>내부 리뷰 서비스의 `/internal/v1/reviews/summary` 엔드포인트에 `storeId` 쿼리 파라미터로 GET 요청을 보내고,
     * 응답 본문을 RatingSummaryResponse로 역직렬화하여 반환합니다.</p>
     *
     * @param storeId 조회할 매장의 UUID
     * @return 해당 매장의 RatingSummaryResponse
     */
    public RatingSummaryResponse fetchSummary(UUID storeId) {
        String uri = UriComponentsBuilder.fromPath("/internal/v1/reviews/summary")
                .queryParam("storeId", storeId)
                .build(true).toUriString();

        return ordersRestClient.get()
                .uri(uri)
                .retrieve()
                .body(RatingSummaryResponse.class);
    }
}
