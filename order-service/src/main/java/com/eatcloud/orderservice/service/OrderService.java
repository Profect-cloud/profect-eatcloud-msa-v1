package com.eatcloud.orderservice.service;

import com.eatcloud.orderservice.dto.CreateOrderRequest;
import com.eatcloud.orderservice.dto.CreateOrderResponse;
import com.eatcloud.orderservice.entity.Order;
import com.eatcloud.orderservice.entity.OrderItem;
import com.eatcloud.orderservice.entity.OrderStatus;
import com.eatcloud.orderservice.event.OrderCreatedEvent;
import com.eatcloud.orderservice.repository.OrderItemRepository;
import com.eatcloud.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventProducer orderEventProducer;
    
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        log.info("주문 생성 시작: customerId={}, storeId={}, totalAmount={}", 
                request.getCustomerId(), request.getStoreId(), request.getTotalAmount());
        
        int finalAmount = request.getTotalAmount() - (request.getPointsToUse() != null ? request.getPointsToUse() : 0);
        
        Order order = Order.builder()
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .storeId(request.getStoreId())
                .totalAmount(request.getTotalAmount())
                .finalAmount(finalAmount)
                .pointsUsed(request.getPointsToUse() != null ? request.getPointsToUse() : 0)
                .orderStatus(OrderStatus.PENDING)
                .orderType(request.getOrderType())
                .deliveryAddress(request.getDeliveryAddress())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> OrderItem.builder()
                        .order(savedOrder)
                        .menuId(itemRequest.getMenuId())
                        .menuName(itemRequest.getMenuName())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(itemRequest.getUnitPrice())
                        .totalPrice(itemRequest.getQuantity() * itemRequest.getUnitPrice())
                        .options(itemRequest.getOptions())
                        .build())
                .collect(Collectors.toList());
        
        orderItemRepository.saveAll(orderItems);
        
        OrderCreatedEvent event = createOrderCreatedEvent(savedOrder, orderItems, request);
        orderEventProducer.publishOrderCreated(event);
        
        log.info("주문 생성 완료: orderId={}", savedOrder.getOrderId());
        
        return CreateOrderResponse.builder()
                .orderId(savedOrder.getOrderId())
                .customerId(savedOrder.getCustomerId())
                .storeId(savedOrder.getStoreId())
                .totalAmount(savedOrder.getTotalAmount())
                .finalAmount(savedOrder.getFinalAmount())
                .pointsUsed(savedOrder.getPointsUsed())
                .orderStatus(savedOrder.getOrderStatus())
                .orderDate(savedOrder.getOrderDate())
                .createdAt(savedOrder.getCreatedAt())
                .message("주문이 성공적으로 생성되었습니다.")
                .build();
    }
    
    private OrderCreatedEvent createOrderCreatedEvent(Order order, List<OrderItem> orderItems, CreateOrderRequest request) {
        List<OrderCreatedEvent.OrderItemEvent> itemEvents = orderItems.stream()
                .map(item -> OrderCreatedEvent.OrderItemEvent.builder()
                        .menuId(item.getMenuId())
                        .menuName(item.getMenuName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .options(item.getOptions())
                        .build())
                .collect(Collectors.toList());
        
        return OrderCreatedEvent.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .storeId(order.getStoreId())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getFinalAmount())
                .pointsToUse(request.getPointsToUse())
                .orderStatus(order.getOrderStatus())
                .orderType(order.getOrderType())
                .orderDate(order.getOrderDate())
                .createdAt(order.getCreatedAt())
                .orderItems(itemEvents)
                .build();
    }
    
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus status) {
        log.info("주문 상태 업데이트: orderId={}, status={}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderId));
        
        order.updateStatus(status);
        orderRepository.save(order);
        
        log.info("주문 상태 업데이트 완료: orderId={}, status={}", orderId, status);
    }
    
    @Transactional
    public void setPaymentId(UUID orderId, UUID paymentId) {
        log.info("주문에 결제 ID 설정: orderId={}, paymentId={}", orderId, paymentId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderId));
        
        order.setPaymentId(paymentId);
        orderRepository.save(order);
        
        log.info("주문에 결제 ID 설정 완료: orderId={}, paymentId={}", orderId, paymentId);
    }
} 