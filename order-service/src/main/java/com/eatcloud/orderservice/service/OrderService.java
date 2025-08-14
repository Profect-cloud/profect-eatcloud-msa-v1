package com.eatcloud.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eatcloud.orderservice.entity.Order;
import com.eatcloud.orderservice.repository.OrderRepository;
import com.eatcloud.orderservice.dto.OrderMenu;
import com.eatcloud.orderservice.entity.OrderStatusCode;
import com.eatcloud.orderservice.entity.OrderTypeCode;
import com.eatcloud.orderservice.repository.OrderStatusCodeRepository;
import com.eatcloud.orderservice.repository.OrderTypeCodeRepository;

import com.eatcloud.orderservice.dto.CartItem;
import com.eatcloud.orderservice.dto.request.CreateOrderRequest;
import com.eatcloud.orderservice.dto.response.CreateOrderResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusCodeRepository orderStatusCodeRepository;
    private final OrderTypeCodeRepository orderTypeCodeRepository;
    private final ExternalApiService externalApiService;
    
    @Autowired
    private CartService cartService;
    
    public CreateOrderResponse createOrderFromCart(UUID customerId, CreateOrderRequest request) {
        List<CartItem> cartItems = cartService.getCart(customerId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("장바구니가 비어있습니다.");
        }
        
        List<OrderMenu> orderMenuList = cartItems.stream()
            .map(item -> OrderMenu.builder()
                .menuId(item.getMenuId())
                .menuName(item.getMenuName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build())
            .collect(Collectors.toList());
        
        Order order = createPendingOrder(
            customerId,
            request.getStoreId(),
            orderMenuList,
            request.getOrderType(),
            request.getUsePoints(),
            request.getPointsToUse()
        );
        
        try {
            cartService.clearCart(customerId);
        } catch (Exception e) {
            log.error("Failed to clear cart after order creation: {}", e.getMessage());
        }
        
        return CreateOrderResponse.builder()
            .orderId(order.getOrderId())
            .orderNumber(order.getOrderNumber())
            .totalPrice(order.getTotalPrice())
            .finalPaymentAmount(order.getFinalPaymentAmount())
            .orderStatus(order.getOrderStatusCode().getCode())
            .message("주문이 생성되었습니다.")
            .build();
    }

    public Order createPendingOrder(UUID customerId, UUID storeId, List<OrderMenu> orderMenuList, String orderType,
                                   Boolean usePoints, Integer pointsToUse) {
        String orderNumber = generateOrderNumber();

        OrderStatusCode statusCode = orderStatusCodeRepository.findByCode("PENDING")
                .orElseThrow(() -> new RuntimeException("주문 상태 코드를 찾을 수 없습니다: PENDING"));
        
        OrderTypeCode typeCode = orderTypeCodeRepository.findByCode(orderType)
                .orElseThrow(() -> new RuntimeException("주문 타입 코드를 찾을 수 없습니다: " + orderType));

        for (OrderMenu orderMenu : orderMenuList) {
            try {
                Integer menuPrice = externalApiService.getMenuPrice(orderMenu.getMenuId());
                orderMenu.setPrice(menuPrice);
            } catch (Exception e) {
                log.error("Failed to get menu price for menuId: {}", orderMenu.getMenuId(), e);
                throw new RuntimeException("메뉴 가격을 조회할 수 없습니다: " + orderMenu.getMenuId());
            }
        }

        Integer totalPrice = calculateTotalAmount(orderMenuList);

        if (usePoints == null) {
            usePoints = false;
        }
        if (pointsToUse == null) {
            pointsToUse = 0;
        }

        Integer finalPaymentAmount = Math.max(totalPrice - pointsToUse, 0);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .orderMenuList(orderMenuList)
                .customerId(customerId)
                .storeId(storeId)
                .orderStatusCode(statusCode)
                .orderTypeCode(typeCode)
                .totalPrice(totalPrice)
                .usePoints(usePoints)
                .pointsToUse(pointsToUse)
                .finalPaymentAmount(finalPaymentAmount)
                .build();

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public void completePayment(UUID orderId, UUID paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderId));

        // 이미 PAID 상태면 중복 처리 방지 (멱등성 보장)
        if ("PAID".equals(order.getOrderStatusCode().getCode())) {
            log.warn("주문이 이미 결제 완료 상태입니다: orderId={}, paymentId={}", orderId, paymentId);
            return;
        }

        // PENDING 상태가 아니면 결제 완료 불가
        if (!"PENDING".equals(order.getOrderStatusCode().getCode())) {
            log.error("결제 완료할 수 없는 주문 상태: orderId={}, currentStatus={}", 
                     orderId, order.getOrderStatusCode().getCode());
            throw new RuntimeException("결제 완료할 수 없는 주문 상태입니다: " + order.getOrderStatusCode().getCode());
        }

        OrderStatusCode paidStatus = orderStatusCodeRepository.findByCode("PAID")
                .orElseThrow(() -> new RuntimeException("주문 상태 코드를 찾을 수 없습니다: PAID"));

        order.setPaymentId(paymentId);
        order.setOrderStatusCode(paidStatus);
        orderRepository.save(order);

        log.info("주문 결제 완료 처리: orderId={}, paymentId={}", orderId, paymentId);

        try {
            externalApiService.invalidateCart(order.getCustomerId());
            log.info("Cart invalidated after successful payment for customer: {}, order: {}",
                    order.getCustomerId(), order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to invalidate cart after payment completion for customer: {}, order: {}",
                    order.getCustomerId(), order.getOrderNumber(), e);
        }
    }

    public void failPayment(UUID orderId, String failureReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderId));

        // PENDING 상태가 아니면 결제 실패 처리 불가
        if (!"PENDING".equals(order.getOrderStatusCode().getCode())) {
            log.warn("결제 실패 처리할 수 없는 주문 상태: orderId={}, currentStatus={}", 
                    orderId, order.getOrderStatusCode().getCode());
            return;
        }

        OrderStatusCode failedStatus = orderStatusCodeRepository.findByCode("PAYMENT_FAILED")
                .orElseThrow(() -> new RuntimeException("주문 상태 코드를 찾을 수 없습니다: PAYMENT_FAILED"));

        order.setOrderStatusCode(failedStatus);
        orderRepository.save(order);

        log.info("주문 결제 실패 처리: orderId={}, reason={}", orderId, failureReason);
    }

    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderId));

        OrderStatusCode canceledStatus = orderStatusCodeRepository.findByCode("CANCELED")
                .orElseThrow(() -> new RuntimeException("주문 상태 코드를 찾을 수 없습니다: CANCELED"));
        order.setOrderStatusCode(canceledStatus);

        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Integer calculateTotalAmount(List<OrderMenu> orderMenuList) {
        return orderMenuList.stream()
                .mapToInt(menu -> menu.getPrice() * menu.getQuantity())
                .sum();
    }

    private String generateOrderNumber() {
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "ORD-" + date + "-" + randomPart;
    }

    public List<Order> findOrdersByCustomer(UUID customerId) {
        return orderRepository.findAllByCustomerId(customerId);
    }

    public Order findOrderByCustomerAndOrderId(UUID customerId, UUID orderId) {
        return orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId)
                .orElseThrow(() -> new RuntimeException("해당 주문이 없습니다."));
    }

    public List<Order> findOrdersByStore(UUID storeId) {
        return orderRepository.findAllByStoreId(storeId);
    }

    public Order findOrderByStoreAndOrderId(UUID storeId, UUID orderId) {
        return orderRepository.findByOrderIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new RuntimeException("해당 매장에 주문이 없습니다."));
    }

    @Transactional
    public void updateOrderStatus(UUID orderId, String statusCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        OrderStatusCode statusCodeEntity = orderStatusCodeRepository.findById(statusCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상태 코드입니다."));

        order.setOrderStatusCode(statusCodeEntity);
    }
}
