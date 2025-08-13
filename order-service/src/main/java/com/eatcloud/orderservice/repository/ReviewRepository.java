package com.eatcloud.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eatcloud.orderservice.entity.Review;
import com.eatcloud.orderservice.common.BaseTimeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, BaseTimeRepository<Review, UUID> {

	boolean existsByOrderOrderIdAndTimeData_DeletedAtIsNull(UUID orderId);

	@Query("SELECT r FROM Review r JOIN r.order o WHERE o.customerId = :customerId AND r.timeData.deletedAt IS NULL ORDER BY r.timeData.createdAt DESC")
	List<Review> findByOrderCustomerIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(@Param("customerId") UUID customerId);

	@Query("SELECT r FROM Review r JOIN r.order o WHERE o.storeId = :storeId AND r.timeData.deletedAt IS NULL ORDER BY r.timeData.createdAt DESC")
	List<Review> findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(@Param("storeId") UUID storeId);

	@Query("SELECT r FROM Review r JOIN r.order o WHERE r.reviewId = :reviewId AND o.customerId = :customerId AND r.timeData.deletedAt IS NULL")
	Optional<Review> findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(
		@Param("reviewId") UUID reviewId,
		@Param("customerId") UUID customerId
	);
}
