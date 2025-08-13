package com.eatcloud.orderservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eatcloud.orderservice.dto.request.OrderStatusUpdateRequest;
import com.eatcloud.orderservice.dto.request.CreateOrderRequest;
import com.eatcloud.orderservice.dto.response.CreateOrderResponse;
import com.eatcloud.orderservice.dto.response.ApiResponse;
import com.eatcloud.orderservice.entity.Order;
import com.eatcloud.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

	private final OrderService orderService;
	
	@PostMapping
	public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
			@RequestHeader("X-User-Id") String userId,
			@RequestBody CreateOrderRequest request) {
		
		UUID customerId = UUID.fromString(userId);
		CreateOrderResponse response = orderService.createOrderFromCart(customerId, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<Map<String, Object>> getOrder(@PathVariable UUID orderId) {
		Map<String, Object> response = new HashMap<>();

		try {
			Optional<Order> order = orderService.findById(orderId);

			if (order.isPresent()) {
				Order foundOrder = order.get();
				response.put("orderId", foundOrder.getOrderId());
				response.put("orderNumber", foundOrder.getOrderNumber());
				response.put("customerId", foundOrder.getCustomerId());
				response.put("storeId", foundOrder.getStoreId());
				response.put("paymentId", foundOrder.getPaymentId());
				response.put("orderMenuList", foundOrder.getOrderMenuList());
				response.put("orderStatus", foundOrder.getOrderStatusCode().getCode());
				response.put("orderType", foundOrder.getOrderTypeCode().getCode());
				response.put("createdAt", foundOrder.getTimeData().getCreatedAt());
				response.put("message", "주문 조회 성공");

				return ResponseEntity.ok(response);
			} else {
				response.put("error", "주문을 찾을 수 없습니다.");
				return ResponseEntity.notFound().build();
			}

		} catch (Exception e) {
			response.put("error", "주문 조회 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}

	@GetMapping("/number/{orderNumber}")
	public ResponseEntity<Map<String, Object>> getOrderByNumber(@PathVariable String orderNumber) {
		Map<String, Object> response = new HashMap<>();

		try {
			Optional<Order> order = orderService.findOrderByNumber(orderNumber);

			if (order.isPresent()) {
				Order foundOrder = order.get();
				response.put("orderId", foundOrder.getOrderId());
				response.put("orderNumber", foundOrder.getOrderNumber());
				response.put("customerId", foundOrder.getCustomerId());
				response.put("storeId", foundOrder.getStoreId());
				response.put("paymentId", foundOrder.getPaymentId());
				response.put("orderMenuList", foundOrder.getOrderMenuList());
				response.put("orderStatus", foundOrder.getOrderStatusCode().getCode());
				response.put("orderType", foundOrder.getOrderTypeCode().getCode());
				response.put("createdAt", foundOrder.getTimeData().getCreatedAt());
				response.put("message", "주문 조회 성공");

				return ResponseEntity.ok(response);
			} else {
				response.put("error", "주문을 찾을 수 없습니다.");
				return ResponseEntity.notFound().build();
			}

		} catch (Exception e) {
			response.put("error", "주문 조회 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}

	@GetMapping("/customers/{customerId}")
	public ResponseEntity<List<Order>> getCustomerOrders(
			@PathVariable UUID customerId,
			@RequestHeader(value = "X-User-Type", required = false) String userType) {
		
		// Gateway에서 전달된 사용자 타입 확인 (ADMIN만 허용)
		if (!"admin".equalsIgnoreCase(userType)) {
			log.warn("Unauthorized access attempt to customer orders. User type: {}", userType);
			return ResponseEntity.status(403).build();
		}
		
		return ResponseEntity.ok(orderService.findOrdersByCustomer(customerId));
	}

	@GetMapping("/customers/{customerId}/orders/{orderId}")
	public ResponseEntity<Order> getCustomerOrderDetail(
			@PathVariable UUID customerId, 
			@PathVariable UUID orderId,
			@RequestHeader(value = "X-User-Type", required = false) String userType) {
		
		if (!"admin".equalsIgnoreCase(userType)) {
			log.warn("Unauthorized access attempt to customer order detail. User type: {}", userType);
			return ResponseEntity.status(403).build();
		}
		
		return ResponseEntity.ok(orderService.findOrderByCustomerAndOrderId(customerId, orderId));
	}

	@GetMapping("/stores/{storeId}")
	public ResponseEntity<List<Order>> getStoreOrders(
			@PathVariable UUID storeId,
			@RequestHeader(value = "X-User-Type", required = false) String userType) {
		
		// Manager 또는 Admin만 접근 가능
		if (!"manager".equalsIgnoreCase(userType) && !"admin".equalsIgnoreCase(userType)) {
			log.warn("Unauthorized access attempt to store orders. User type: {}", userType);
			return ResponseEntity.status(403).build();
		}
		
		return ResponseEntity.ok(orderService.findOrdersByStore(storeId));
	}

	@GetMapping("/stores/{storeId}/orders/{orderId}")
	public ResponseEntity<Order> getStoreOrderDetail(
			@PathVariable UUID orderId, 
			@PathVariable UUID storeId,
			@RequestHeader(value = "X-User-Type", required = false) String userType) {
		
		if (!"manager".equalsIgnoreCase(userType) && !"admin".equalsIgnoreCase(userType)) {
			log.warn("Unauthorized access attempt to store order detail. User type: {}", userType);
			return ResponseEntity.status(403).build();
		}
		
		return ResponseEntity.ok(orderService.findOrderByStoreAndOrderId(storeId, orderId));
	}

	@PatchMapping("/{orderId}/status")
	public ResponseEntity<Void> updateOrderStatus(
			@PathVariable UUID orderId,
			@RequestBody @Valid OrderStatusUpdateRequest request,
			@RequestHeader(value = "X-User-Type", required = false) String userType) {
		
		if (!"admin".equalsIgnoreCase(userType)) {
			log.warn("Unauthorized attempt to update order status. User type: {}", userType);
			return ResponseEntity.status(403).build();
		}
		
		orderService.updateOrderStatus(orderId, request.getStatusCode());
		return ResponseEntity.noContent().build();
	}
}
