package com.eatcloud.orderservice.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.eatcloud.orderservice.dto.request.ReviewRequestDto;
import com.eatcloud.orderservice.dto.response.ReviewResponseDto;
import com.eatcloud.orderservice.service.ReviewService;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	// 리뷰 작성
	@PostMapping
	public ResponseEntity<ReviewResponseDto> createReview(
		@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody ReviewRequestDto request) {
		
		UUID customerId = UUID.fromString(userId);
		ReviewResponseDto response = reviewService.createReview(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 고객별 리뷰 조회 (내 리뷰 목록)
	@GetMapping
	public ResponseEntity<List<ReviewResponseDto>> getMyReviews(
		@RequestHeader("X-User-Id") String userId) {
		
		UUID customerId = UUID.fromString(userId);
		List<ReviewResponseDto> reviews = reviewService.getReviewsByCustomer(customerId);
		return ResponseEntity.ok(reviews);
	}

	// 리뷰 수정 (새로 추가)
	@PutMapping("/{reviewId}")
	public ResponseEntity<ReviewResponseDto> updateReview(
		@RequestHeader("X-User-Id") String userId,
		@PathVariable UUID reviewId,
		@Valid @RequestBody ReviewRequestDto request) {
		
		UUID customerId = UUID.fromString(userId);
		ReviewResponseDto response = reviewService.updateReview(customerId, reviewId, request);
		return ResponseEntity.ok(response);
	}

	// 리뷰 삭제
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview(
		@RequestHeader("X-User-Id") String userId,
		@PathVariable UUID reviewId) {
		
		UUID customerId = UUID.fromString(userId);
		reviewService.deleteReview(customerId, reviewId);
		return ResponseEntity.noContent().build();
	}

	// 매장별 리뷰 조회 (기존 기능 유지)
	@GetMapping("/stores/{storeId}")
	public ResponseEntity<List<ReviewResponseDto>> getReviewsByStore(@PathVariable UUID storeId) {
		List<ReviewResponseDto> reviews = reviewService.getReviewsByStore(storeId);
		return ResponseEntity.ok(reviews);
	}

	// 매장별 평점별 리뷰 필터링 (새로 추가)
	@GetMapping("/stores/{storeId}/filter")
	public ResponseEntity<List<ReviewResponseDto>> getReviewsByStoreAndRating(
		@PathVariable UUID storeId,
		@RequestParam BigDecimal rating) {
		
		List<ReviewResponseDto> reviews = reviewService.getReviewsByStoreAndRating(storeId, rating);
		return ResponseEntity.ok(reviews);
	}

	// 매장 평점 평균 계산 (새로 추가)
	@GetMapping("/stores/{storeId}/average-rating")
	public ResponseEntity<BigDecimal> getAverageRating(@PathVariable UUID storeId) {
		BigDecimal averageRating = reviewService.calculateAverageRating(storeId);
		return ResponseEntity.ok(averageRating);
	}

	// 매장 리뷰 통계 (새로 추가)
	@GetMapping("/stores/{storeId}/statistics")
	public ResponseEntity<Map<String, Object>> getReviewStatistics(@PathVariable UUID storeId) {
		Map<String, Object> statistics = reviewService.getReviewStatistics(storeId);
		return ResponseEntity.ok(statistics);
	}
}
