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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final OrderRepository orderRepository;
	private final ExternalApiService externalApiService;

	@Transactional
	public ReviewResponseDto createReview(UUID customerId, ReviewRequestDto request) {
		Order order = validateAndGetOrder(customerId, request.orderId());

		Review review = Review.builder()
			.order(order)
			.rating(request.rating())
			.content(request.content())
			.build();

		Review savedReview = reviewRepository.save(review);
		return toResponse(savedReview);
	}

	public List<ReviewResponseDto> getReviewListByCustomer(UUID customerId) {
		List<Review> reviews = reviewRepository
			.findByOrderCustomerIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(customerId);

		return reviews.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
	}

	public List<ReviewResponseDto> getReviewListByStore(UUID storeId) {
		List<Review> reviews = reviewRepository
			.findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId);

		return reviews.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
	}

	public ReviewResponseDto getReview(UUID customerId, UUID reviewId) {
		Review review = reviewRepository.findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId)
			.orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없거나 접근 권한이 없습니다."));

		return toResponse(review);
	}

	@Transactional
	public void deleteReview(UUID customerId, UUID reviewId) {
		Review review = reviewRepository.findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId)
			.orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없거나 접근 권한이 없습니다."));
		reviewRepository.deleteById(reviewId);
	}

	private Order validateAndGetOrder(UUID customerId, UUID orderId) {
		Boolean customerExists = externalApiService.checkCustomerExists(customerId);
		if (!customerExists) {
			throw new IllegalArgumentException("Customer not found: " + customerId);
		}

		Order order = orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId)
			.orElseThrow(() -> new IllegalArgumentException("Order not found or not authorized"));

		if (reviewRepository.existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId)) {
			throw new IllegalArgumentException("이미 해당 주문에 대한 리뷰가 존재합니다.");
		}

		return order;
	}

	private ReviewResponseDto toResponse(Review review) {
		return new ReviewResponseDto(
			review.getReviewId(),
			review.getOrder().getOrderId(),
			review.getRating(),
			review.getContent(),
			review.getTimeData().getCreatedAt()
		);
	}
}
