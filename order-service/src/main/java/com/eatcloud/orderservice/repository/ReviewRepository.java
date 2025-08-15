package com.eatcloud.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eatcloud.orderservice.entity.Review;
import com.eatcloud.orderservice.common.BaseTimeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends BaseTimeRepository<Review, UUID> {

	// 주문별 리뷰 존재 여부 확인
	boolean existsByOrderOrderIdAndTimeData_DeletedAtIsNull(UUID orderId);

	// 고객별 리뷰 조회
	@Query("SELECT r FROM Review r JOIN r.order o WHERE o.customerId = :customerId AND r.timeData.deletedAt IS NULL ORDER BY r.timeData.createdAt DESC")
	List<Review> findByOrderCustomerIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(@Param("customerId") UUID customerId);

	// 매장별 리뷰 조회
	@Query("SELECT r FROM Review r JOIN r.order o WHERE o.storeId = :storeId AND r.timeData.deletedAt IS NULL ORDER BY r.timeData.createdAt DESC")
	List<Review> findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(@Param("storeId") UUID storeId);

	// 리뷰 ID와 고객 ID로 리뷰 조회 (권한 확인용)
	@Query("SELECT r FROM Review r JOIN r.order o WHERE r.reviewId = :reviewId AND o.customerId = :customerId AND r.timeData.deletedAt IS NULL")
	Optional<Review> findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(
		@Param("reviewId") UUID reviewId,
		@Param("customerId") UUID customerId
	);

	// 매장별 평점별 리뷰 조회 (필터링용)
	@Query("SELECT r FROM Review r JOIN r.order o WHERE o.storeId = :storeId AND r.rating = :rating AND r.timeData.deletedAt IS NULL ORDER BY r.timeData.createdAt DESC")
	List<Review> findByOrderStoreIdAndRatingAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(
		@Param("storeId") UUID storeId, 
		@Param("rating") BigDecimal rating
	);
}
