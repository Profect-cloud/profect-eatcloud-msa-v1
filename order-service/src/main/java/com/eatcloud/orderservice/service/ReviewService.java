package com.eatcloud.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatcloud.orderservice.dto.request.ReviewRequestDto;
import com.eatcloud.orderservice.dto.response.ReviewResponseDto;
import com.eatcloud.orderservice.entity.Order;
import com.eatcloud.orderservice.entity.Review;
import com.eatcloud.orderservice.repository.OrderRepository;
import com.eatcloud.orderservice.repository.ReviewRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final OrderRepository orderRepository;

	@Transactional
	public ReviewResponseDto createReview(UUID customerId, ReviewRequestDto request) {
		// 유효성 검증
		validateReviewRequest(request);
		
		// 주문 검증
		Order order = validateAndGetOrder(customerId, request.orderId());

		// 리뷰 생성
		Review review = Review.builder()
			.order(order)
			.rating(request.rating())
			.content(request.content())
			.build();

		Review savedReview = reviewRepository.save(review);
		return toResponse(savedReview);
	}

	public List<ReviewResponseDto> getReviewsByCustomer(UUID customerId) {
		List<Review> reviews = reviewRepository
			.findByOrderCustomerIdAndDeletedAtIsNullOrderByCreatedAtDesc(customerId);

		return reviews.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
	}

	public List<ReviewResponseDto> getReviewsByStore(UUID storeId) {
		List<Review> reviews = reviewRepository
			.findByOrderStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(storeId);

		return reviews.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
	}

	public List<ReviewResponseDto> getReviewsByStoreAndRating(UUID storeId, BigDecimal rating) {
		List<Review> reviews = reviewRepository
			.findByOrderStoreIdAndRatingAndDeletedAtIsNullOrderByCreatedAtDesc(storeId, rating);

		return reviews.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
	}

	@Transactional
	public ReviewResponseDto updateReview(UUID customerId, UUID reviewId, ReviewRequestDto request) {
		// 유효성 검증
		validateReviewRequest(request);
		
		// 리뷰 조회 및 권한 확인
		Review review = reviewRepository.findByReviewIdAndOrderCustomerIdAndDeletedAtIsNull(reviewId, customerId)
			.orElseThrow(() -> new RuntimeException("해당 리뷰가 없거나 수정 권한이 없습니다."));

		// 리뷰 수정 (Setter가 없으므로 새로 빌드)
		Review updatedReview = Review.builder()
			.reviewId(review.getReviewId())
			.order(review.getOrder())
			.rating(request.rating())
			.content(request.content())
			.build();

		Review savedReview = reviewRepository.save(updatedReview);
		return toResponse(savedReview);
	}

	@Transactional
	public void deleteReview(UUID customerId, UUID reviewId) {
		Review review = reviewRepository.findByReviewIdAndOrderCustomerIdAndDeletedAtIsNull(reviewId, customerId)
			.orElseThrow(() -> new RuntimeException("해당 리뷰가 없거나 삭제 권한이 없습니다."));
		
		reviewRepository.delete(review);
	}

	public BigDecimal calculateAverageRating(UUID storeId) {
		List<Review> reviews = reviewRepository
			.findByOrderStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(storeId);

		if (reviews.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal sum = reviews.stream()
			.map(Review::getRating)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		return sum.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);
	}

	public Map<String, Object> getReviewStatistics(UUID storeId) {
		List<Review> reviews = reviewRepository
			.findByOrderStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(storeId);

		Map<String, Object> statistics = new HashMap<>();
		
		// 총 리뷰 수
		statistics.put("totalReviews", reviews.size());
		
		// 평균 평점
		statistics.put("averageRating", calculateAverageRating(storeId));
		
		// 평점별 개수
		statistics.put("fiveStarCount", countByRating(reviews, new BigDecimal("5.0")));
		statistics.put("fourStarCount", countByRating(reviews, new BigDecimal("4.0")));
		statistics.put("threeStarCount", countByRating(reviews, new BigDecimal("3.0")));
		statistics.put("twoStarCount", countByRating(reviews, new BigDecimal("2.0")));
		statistics.put("oneStarCount", countByRating(reviews, new BigDecimal("1.0")));

		return statistics;
	}

	private void validateReviewRequest(ReviewRequestDto request) {
		// 평점 유효성 검증
		if (request.rating().compareTo(new BigDecimal("1.0")) < 0 || 
			request.rating().compareTo(new BigDecimal("5.0")) > 0) {
			throw new RuntimeException("평점은 1.0~5.0 사이의 값이어야 합니다.");
		}

		// 내용 유효성 검증
		if (request.content() == null || request.content().trim().isEmpty()) {
			throw new RuntimeException("리뷰 내용은 필수입니다.");
		}

		// 내용 길이 검증
		if (request.content().length() > 1000) {
			throw new RuntimeException("리뷰 내용은 1000자 이하로 작성해주세요.");
		}

		// 부적절한 내용 검증
		if (containsInappropriateContent(request.content())) {
			throw new RuntimeException("부적절한 내용이 포함되어 있습니다.");
		}
	}

	private boolean containsInappropriateContent(String content) {
		List<String> inappropriateWords = Arrays.asList("바보", "멍청이", "짜증", "최악");
		return inappropriateWords.stream()
			.anyMatch(word -> content.contains(word));
	}

	private long countByRating(List<Review> reviews, BigDecimal rating) {
		return reviews.stream()
			.filter(review -> review.getRating().compareTo(rating) == 0)
			.count();
	}

	private Order validateAndGetOrder(UUID customerId, UUID orderId) {
		Order order = orderRepository.findByOrderIdAndCustomerIdAndDeletedAtIsNull(orderId, customerId)
			.orElseThrow(() -> new RuntimeException("해당 주문이 없거나 권한이 없습니다."));

		if (reviewRepository.existsByOrderOrderIdAndDeletedAtIsNull(orderId)) {
			throw new RuntimeException("이미 해당 주문에 대한 리뷰가 존재합니다.");
		}

		return order;
	}

	private ReviewResponseDto toResponse(Review review) {
		return new ReviewResponseDto(
			review.getReviewId(),
			review.getOrder().getOrderId(),
			review.getRating(),
			review.getContent(),
									review.getCreatedAt()
		);
	}
}
