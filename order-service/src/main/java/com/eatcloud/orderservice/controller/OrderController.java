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
import com.eatcloud.orderservice.dto.request.PaymentCompleteRequest;
import com.eatcloud.orderservice.dto.request.PaymentFailedRequest;
import com.eatcloud.orderservice.dto.response.CreateOrderResponse;
import com.eatcloud.orderservice.dto.response.ApiResponse;
import com.eatcloud.orderservice.entity.Order;
import com.eatcloud.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Service", description = "주문 서비스 API")
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
			@RequestHeader("X-User-Id") String userId,
			@RequestBody CreateOrderRequest request) {

		try {
			UUID customerId = UUID.fromString(userId);
			CreateOrderResponse response = orderService.createOrderFromCart(customerId, request);
			return ResponseEntity.ok(ApiResponse.success(response));
		} catch (IllegalArgumentException e) {
			log.error("Invalid user ID format: {}", userId);
			return ResponseEntity.badRequest()
				.body(ApiResponse.error("유효하지 않은 사용자 ID입니다."));
		}
		// OrderException과 CartException은 GlobalExceptionHandler에서 처리
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

	// ===== Payment Service 콜백 엔드포인트들 =====

	@PostMapping("/{orderId}/payment/complete")
	public ResponseEntity<Map<String, Object>> completePayment(
			@PathVariable UUID orderId,
			@RequestBody @Valid PaymentCompleteRequest request,
			@RequestHeader(value = "X-Service-Name", required = false) String serviceName) {

		Map<String, Object> response = new HashMap<>();

		try {
			// Payment Service에서만 호출 허용
			if (!"payment-service".equals(serviceName)) {
				log.warn("Unauthorized payment completion attempt from: {}", serviceName);
				response.put("error", "Unauthorized service");
				return ResponseEntity.status(403).body(response);
			}

			log.info("결제 완료 콜백 수신: orderId={}, paymentId={}", orderId, request.getPaymentId());

			orderService.completePayment(orderId, request.getPaymentId());

			response.put("message", "주문 결제 완료 처리되었습니다");
			response.put("orderId", orderId);
			response.put("paymentId", request.getPaymentId());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("결제 완료 처리 중 오류: orderId={}, paymentId={}", orderId, request.getPaymentId(), e);
			response.put("error", "결제 완료 처리 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}

	@PostMapping("/{orderId}/payment/failed")
	public ResponseEntity<Map<String, Object>> failPayment(
			@PathVariable UUID orderId,
			@RequestBody @Valid PaymentFailedRequest request,
			@RequestHeader(value = "X-Service-Name", required = false) String serviceName) {

		Map<String, Object> response = new HashMap<>();

		try {
			// Payment Service에서만 호출 허용
			if (!"payment-service".equals(serviceName)) {
				log.warn("Unauthorized payment failure attempt from: {}", serviceName);
				response.put("error", "Unauthorized service");
				return ResponseEntity.status(403).body(response);
			}

			log.info("결제 실패 콜백 수신: orderId={}, reason={}", orderId, request.getFailureReason());

			orderService.failPayment(orderId, request.getFailureReason());

			response.put("message", "주문 결제 실패 처리되었습니다");
			response.put("orderId", orderId);
			response.put("failureReason", request.getFailureReason());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("결제 실패 처리 중 오류: orderId={}, reason={}", orderId, request.getFailureReason(), e);
			response.put("error", "결제 실패 처리 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}

	@PostMapping("/{orderId}/payment/cancel")
	public ResponseEntity<Map<String, Object>> cancelPayment(
			@PathVariable UUID orderId,
			@RequestHeader(value = "X-Service-Name", required = false) String serviceName) {

		Map<String, Object> response = new HashMap<>();

		try {
			// Payment Service에서만 호출 허용
			if (!"payment-service".equals(serviceName)) {
				log.warn("Unauthorized payment cancellation attempt from: {}", serviceName);
				response.put("error", "Unauthorized service");
				return ResponseEntity.status(403).body(response);
			}

			log.info("결제 취소 콜백 수신: orderId={}", orderId);

			orderService.cancelOrder(orderId);

			response.put("message", "주문이 취소되었습니다");
			response.put("orderId", orderId);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("결제 취소 처리 중 오류: orderId={}", orderId, e);
			response.put("error", "결제 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}
}
