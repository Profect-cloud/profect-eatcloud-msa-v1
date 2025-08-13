package com.eatcloud.orderservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@PostMapping
	public ResponseEntity<ReviewResponseDto> createReview(
		@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody ReviewRequestDto request) {
		
		UUID customerId = UUID.fromString(userId);
		ReviewResponseDto response = reviewService.createReview(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ReviewResponseDto>> getReviewList(
		@RequestHeader("X-User-Id") String userId) {
		
		UUID customerId = UUID.fromString(userId);
		List<ReviewResponseDto> reviews = reviewService.getReviewListByCustomer(customerId);
		return ResponseEntity.ok(reviews);
	}

	@GetMapping("/{reviewId}")
	public ResponseEntity<ReviewResponseDto> getReview(
		@RequestHeader("X-User-Id") String userId,
		@PathVariable UUID reviewId) {
		
		UUID customerId = UUID.fromString(userId);
		ReviewResponseDto response = reviewService.getReview(customerId, reviewId);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview(
		@RequestHeader("X-User-Id") String userId,
		@PathVariable UUID reviewId) {
		
		UUID customerId = UUID.fromString(userId);
		reviewService.deleteReview(customerId, reviewId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/stores/{storeId}")
	public ResponseEntity<List<ReviewResponseDto>> getReviewsByStore(@PathVariable UUID storeId) {
		List<ReviewResponseDto> reviews = reviewService.getReviewListByStore(storeId);
		return ResponseEntity.ok(reviews);
	}
}
