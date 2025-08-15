package com.eatcloud.orderservice.service;

import com.eatcloud.orderservice.dto.request.ReviewRequestDto;
import com.eatcloud.orderservice.dto.response.ReviewResponseDto;
import com.eatcloud.orderservice.entity.Order;
import com.eatcloud.orderservice.entity.Review;
import com.eatcloud.orderservice.repository.OrderRepository;
import com.eatcloud.orderservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID customerId;
    private UUID orderId;
    private UUID storeId;
    private UUID reviewId;
    private Order order;
    private Review review;
    private ReviewRequestDto reviewRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        // 주문 엔티티 설정
        order = Order.builder()
                .orderId(orderId)
                .customerId(customerId)
                .storeId(storeId)
                .orderNumber("ORD-20241215-ABCDE")
                .build();

        // 리뷰 요청 DTO 설정 (record 타입 - 생성자 방식)
        reviewRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("5.0"),
                "정말 맛있었습니다!"
        );

        // 리뷰 엔티티 설정 (Order 엔티티 참조)
        review = Review.builder()
                .reviewId(reviewId)
                .order(order)  // Order 엔티티 참조
                .rating(new BigDecimal("5.0"))
                .content("정말 맛있었습니다!")
                .build();
    }

    @Test
    @DisplayName("리뷰 작성 - 성공")
    void createReview_Success() {
        // Given
        given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
                .willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(review);

        // When
        ReviewResponseDto response = reviewService.createReview(customerId, reviewRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.reviewId()).isEqualTo(reviewId);
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.rating()).isEqualTo(new BigDecimal("5.0"));
        assertThat(response.content()).isEqualTo("정말 맛있었습니다!");

        verify(orderRepository).findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId);
        verify(reviewRepository).existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 - 주문 없음 예외")
    void createReview_OrderNotFound_ThrowsException() {
        // Given
        given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(customerId, reviewRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 주문이 없거나 권한이 없습니다.");

        verify(orderRepository).findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 - 이미 작성된 리뷰 예외")
    void createReview_AlreadyExists_ThrowsException() {
        // Given
        given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
                .willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(customerId, reviewRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 해당 주문에 대한 리뷰가 존재합니다.");

        verify(reviewRepository).existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 - 잘못된 평점 (0.5점)")
    void createReview_InvalidRatingLow_ThrowsException() {
        // Given
        ReviewRequestDto invalidRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("0.5"),  // 1.0보다 낮은 평점
                "평점이 잘못됨"
        );

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(customerId, invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("평점은 1.0~5.0 사이의 값이어야 합니다.");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 - 잘못된 평점 (5.5점)")
    void createReview_InvalidRatingHigh_ThrowsException() {
        // Given
        ReviewRequestDto invalidRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("5.5"),  // 5.0보다 높은 평점
                "평점이 잘못됨"
        );

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(customerId, invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("평점은 1.0~5.0 사이의 값이어야 합니다.");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 - 빈 내용")
    void createReview_EmptyContent_ThrowsException() {
        // Given
        ReviewRequestDto invalidRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("5.0"),
                ""  // 빈 내용
        );

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(customerId, invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("리뷰 내용은 필수입니다.");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 - null 내용")
    void createReview_NullContent_ThrowsException() {
        // Given
        ReviewRequestDto invalidRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("5.0"),
                null  // null 내용
        );

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(customerId, invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("리뷰 내용은 필수입니다.");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("매장별 리뷰 조회 - 성공")
    void getReviewsByStore_Success() {
        // Given
        Order order1 = Order.builder().orderId(UUID.randomUUID()).storeId(storeId).build();
        Order order2 = Order.builder().orderId(UUID.randomUUID()).storeId(storeId).build();

        List<Review> storeReviews = Arrays.asList(
                Review.builder()
                        .reviewId(UUID.randomUUID())
                        .order(order1)
                        .rating(new BigDecimal("5.0"))
                        .content("맛있어요!")
                        .build(),
                Review.builder()
                        .reviewId(UUID.randomUUID())
                        .order(order2)
                        .rating(new BigDecimal("4.0"))
                        .content("좋아요!")
                        .build()
        );

        given(reviewRepository.findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId))
                .willReturn(storeReviews);

        // When
        List<ReviewResponseDto> responses = reviewService.getReviewsByStore(storeId);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).rating()).isEqualTo(new BigDecimal("5.0"));
        assertThat(responses.get(0).content()).isEqualTo("맛있어요!");
        assertThat(responses.get(1).rating()).isEqualTo(new BigDecimal("4.0"));
        assertThat(responses.get(1).content()).isEqualTo("좋아요!");

        verify(reviewRepository).findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId);
    }

    @Test
    @DisplayName("고객별 리뷰 조회 - 성공")
    void getReviewsByCustomer_Success() {
        // Given
        Order customerOrder1 = Order.builder().orderId(UUID.randomUUID()).customerId(customerId).build();
        Order customerOrder2 = Order.builder().orderId(UUID.randomUUID()).customerId(customerId).build();

        List<Review> customerReviews = Arrays.asList(
                Review.builder()
                        .reviewId(UUID.randomUUID())
                        .order(customerOrder1)
                        .rating(new BigDecimal("5.0"))
                        .content("맛있었어요!")
                        .build(),
                Review.builder()
                        .reviewId(UUID.randomUUID())
                        .order(customerOrder2)
                        .rating(new BigDecimal("4.0"))
                        .content("괜찮았어요!")
                        .build()
        );

        given(reviewRepository.findByOrderCustomerIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(customerId))
                .willReturn(customerReviews);

        // When
        List<ReviewResponseDto> responses = reviewService.getReviewsByCustomer(customerId);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).rating()).isEqualTo(new BigDecimal("5.0"));
        assertThat(responses.get(0).content()).isEqualTo("맛있었어요!");

        verify(reviewRepository).findByOrderCustomerIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(customerId);
    }

    @Test
    @DisplayName("리뷰 수정 - 성공")
    void updateReview_Success() {
        // Given
        ReviewRequestDto updateRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("4.0"),
                "수정된 리뷰 내용입니다."
        );

        Review updatedReview = Review.builder()
                .reviewId(reviewId)
                .order(order)
                .rating(new BigDecimal("4.0"))
                .content("수정된 리뷰 내용입니다.")
                .build();

        given(reviewRepository.findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId))
                .willReturn(Optional.of(review));
        given(reviewRepository.save(any(Review.class))).willReturn(updatedReview);

        // When
        ReviewResponseDto response = reviewService.updateReview(customerId, reviewId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.reviewId()).isEqualTo(reviewId);
        assertThat(response.rating()).isEqualTo(new BigDecimal("4.0"));
        assertThat(response.content()).isEqualTo("수정된 리뷰 내용입니다.");

        verify(reviewRepository).findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 수정 - 리뷰 없음 또는 권한 없음")
    void updateReview_NotFoundOrNoPermission_ThrowsException() {
        // Given
        ReviewRequestDto updateRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("4.0"),
                "수정된 리뷰 내용입니다."
        );

        given(reviewRepository.findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.updateReview(customerId, reviewId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 리뷰가 없거나 수정 권한이 없습니다.");

        verify(reviewRepository).findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 삭제 - 성공")
    void deleteReview_Success() {
        // Given
        given(reviewRepository.findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId))
                .willReturn(Optional.of(review));

        // When
        reviewService.deleteReview(customerId, reviewId);

        // Then
        verify(reviewRepository).findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId);
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("리뷰 삭제 - 리뷰 없음 또는 권한 없음")
    void deleteReview_NotFoundOrNoPermission_ThrowsException() {
        // Given
        given(reviewRepository.findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.deleteReview(customerId, reviewId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 리뷰가 없거나 삭제 권한이 없습니다.");

        verify(reviewRepository).findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId);
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    @DisplayName("매장 평점 평균 계산 - 성공")
    void calculateAverageRating_Success() {
        // Given - 매장별 리뷰 조회 메소드를 재사용
        List<Review> storeReviews = Arrays.asList(
                Review.builder().rating(new BigDecimal("5.0")).build(),
                Review.builder().rating(new BigDecimal("4.0")).build(),
                Review.builder().rating(new BigDecimal("3.0")).build(),
                Review.builder().rating(new BigDecimal("5.0")).build()
        );

        given(reviewRepository.findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId))
                .willReturn(storeReviews);

        // When
        BigDecimal averageRating = reviewService.calculateAverageRating(storeId);

        // Then
        assertThat(averageRating).isEqualByComparingTo(new BigDecimal("4.25")); // (5+4+3+5)/4 = 4.25

        verify(reviewRepository).findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId);
    }

    @Test
    @DisplayName("매장 평점 평균 계산 - 리뷰 없음")
    void calculateAverageRating_NoReviews_ReturnsZero() {
        // Given
        given(reviewRepository.findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId))
                .willReturn(Arrays.asList());

        // When
        BigDecimal averageRating = reviewService.calculateAverageRating(storeId);

        // Then
        assertThat(averageRating).isEqualByComparingTo(BigDecimal.ZERO);

        verify(reviewRepository).findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId);
    }

    @Test
    @DisplayName("매장 리뷰 통계 - 평점별 개수")
    void getReviewStatistics_Success() {
        // Given - 매장별 리뷰 조회 메소드를 재사용
        List<Review> allReviews = Arrays.asList(
                Review.builder().rating(new BigDecimal("5.0")).build(),
                Review.builder().rating(new BigDecimal("5.0")).build(),
                Review.builder().rating(new BigDecimal("4.0")).build(),
                Review.builder().rating(new BigDecimal("3.0")).build(),
                Review.builder().rating(new BigDecimal("2.0")).build()
        );

        given(reviewRepository.findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId))
                .willReturn(allReviews);

        // When
        Map<String, Object> statistics = reviewService.getReviewStatistics(storeId);

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.get("totalReviews")).isEqualTo(5);
        assertThat((BigDecimal) statistics.get("averageRating")).isEqualByComparingTo(new BigDecimal("3.80")); // (5+5+4+3+2)/5
        assertThat(statistics.get("fiveStarCount")).isEqualTo(2);
        assertThat(statistics.get("fourStarCount")).isEqualTo(1);
        assertThat(statistics.get("threeStarCount")).isEqualTo(1);
        assertThat(statistics.get("twoStarCount")).isEqualTo(1);
        assertThat(statistics.get("oneStarCount")).isEqualTo(0);

        verify(reviewRepository).findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId);
    }

    @Test
    @DisplayName("리뷰 내용 길이 검증 - 너무 긴 내용")
    void createReview_ContentTooLong_ThrowsException() {
        // Given
        String longContent = "a".repeat(1001); // 1000자 초과
        ReviewRequestDto longContentRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("5.0"),
                longContent
        );

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(customerId, longContentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("리뷰 내용은 1000자 이하로 작성해주세요.");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("부적절한 내용 필터링 테스트")
    void createReview_ContainsInappropriateContent_ThrowsException() {
        // Given
        ReviewRequestDto inappropriateRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("1.0"),
                "이 음식은 정말 바보같아요"
        );

        given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
                .willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(customerId, inappropriateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("부적절한 내용이 포함되어 있습니다.");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("평점 경계값 테스트 - 1.0점")
    void createReview_RatingBoundaryMinimum_Success() {
        // Given
        ReviewRequestDto minRatingRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("1.0"),  // 최소값
                "최소 평점 테스트"
        );

        Review savedReview = Review.builder()
                .reviewId(reviewId)
                .order(order)
                .rating(new BigDecimal("1.0"))
                .content("최소 평점 테스트")
                .build();

        given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
                .willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // When
        ReviewResponseDto response = reviewService.createReview(customerId, minRatingRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(new BigDecimal("1.0"));

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("평점 경계값 테스트 - 5.0점")
    void createReview_RatingBoundaryMaximum_Success() {
        // Given
        ReviewRequestDto maxRatingRequest = new ReviewRequestDto(
                orderId,
                new BigDecimal("5.0"),  // 최대값
                "최대 평점 테스트"
        );

        Review savedReview = Review.builder()
                .reviewId(reviewId)
                .order(order)
                .rating(new BigDecimal("5.0"))
                .content("최대 평점 테스트")
                .build();

        given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
                .willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // When
        ReviewResponseDto response = reviewService.createReview(customerId, maxRatingRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(new BigDecimal("5.0"));

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("매장별 리뷰 조회 - 빈 결과")
    void getReviewsByStore_EmptyResult() {
        // Given
        given(reviewRepository.findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId))
                .willReturn(Arrays.asList());

        // When
        List<ReviewResponseDto> responses = reviewService.getReviewsByStore(storeId);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(reviewRepository).findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId);
    }

    @Test
    @DisplayName("고객별 리뷰 조회 - 빈 결과")
    void getReviewsByCustomer_EmptyResult() {
        // Given
        given(reviewRepository.findByOrderCustomerIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(customerId))
                .willReturn(Arrays.asList());

        // When
        List<ReviewResponseDto> responses = reviewService.getReviewsByCustomer(customerId);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(reviewRepository).findByOrderCustomerIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(customerId);
    }
}