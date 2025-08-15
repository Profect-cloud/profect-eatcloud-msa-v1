package com.eatcloud.orderservice.service;

import com.eatcloud.orderservice.dto.CartItem;
import com.eatcloud.orderservice.dto.OrderMenu;
import com.eatcloud.orderservice.dto.request.CreateOrderRequest;
import com.eatcloud.orderservice.dto.response.CreateOrderResponse;
import com.eatcloud.orderservice.entity.Order;
import com.eatcloud.orderservice.entity.OrderStatusCode;
import com.eatcloud.orderservice.entity.OrderTypeCode;
import com.eatcloud.orderservice.repository.OrderRepository;
import com.eatcloud.orderservice.repository.OrderStatusCodeRepository;
import com.eatcloud.orderservice.repository.OrderTypeCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusCodeRepository orderStatusCodeRepository;

    @Mock
    private OrderTypeCodeRepository orderTypeCodeRepository;

    @Mock
    private ExternalApiService externalApiService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private UUID customerId;
    private UUID orderId;
    private UUID paymentId;
    private CreateOrderRequest createOrderRequest;
    private OrderStatusCode pendingStatus;
    private OrderStatusCode paidStatus;
    private OrderTypeCode deliveryType;
    private Order order;
    private List<CartItem> cartItems;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        paymentId = UUID.randomUUID();

        // 주문 요청 데이터 설정
        createOrderRequest = CreateOrderRequest.builder()
                .storeId(storeId)
                .orderType("DELIVERY")
                .usePoints(false)
                .pointsToUse(0)
                .build();

        // 상태 코드 설정
        pendingStatus = OrderStatusCode.builder()
                .code("PENDING")
                .displayName("대기중")
                .build();

        paidStatus = OrderStatusCode.builder()
                .code("PAID")
                .displayName("결제완료")
                .build();

        // 주문 타입 설정
        deliveryType = OrderTypeCode.builder()
                .code("DELIVERY")
                .displayName("배달")
                .build();

        // 장바구니 아이템 설정
        cartItems = Arrays.asList(
                CartItem.builder()
                        .menuId(UUID.randomUUID())
                        .menuName("김치찌개")
                        .quantity(2)
                        .price(8000)
                        .build(),
                CartItem.builder()
                        .menuId(UUID.randomUUID())
                        .menuName("된장찌개")
                        .quantity(1)
                        .price(7000)
                        .build()
        );

        // 주문 엔티티 설정
        order = Order.builder()
                .orderId(orderId)
                .orderNumber("ORD-20241215-ABCDE")
                .customerId(customerId)
                .storeId(storeId)
                .orderStatusCode(pendingStatus)
                .orderTypeCode(deliveryType)
                .totalPrice(23000)
                .usePoints(false)
                .pointsToUse(0)
                .finalPaymentAmount(23000)
                .build();
    }

    @Test
    @DisplayName("장바구니에서 주문 생성 - 성공")
    void createOrderFromCart_Success() {
        // Given
        given(cartService.getCart(customerId)).willReturn(cartItems);
        given(orderStatusCodeRepository.findByCode("PENDING")).willReturn(Optional.of(pendingStatus));
        given(orderTypeCodeRepository.findByCode("DELIVERY")).willReturn(Optional.of(deliveryType));
        given(externalApiService.getMenuPrice(any(UUID.class))).willReturn(8000, 7000);
        given(orderRepository.save(any(Order.class))).willReturn(order);

        // When
        CreateOrderResponse response = orderService.createOrderFromCart(customerId, createOrderRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getTotalPrice()).isEqualTo(23000);
        assertThat(response.getFinalPaymentAmount()).isEqualTo(23000);
        assertThat(response.getOrderStatus()).isEqualTo("PENDING");
        
        verify(cartService).getCart(customerId);
        verify(cartService).clearCart(customerId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("장바구니에서 주문 생성 - 빈 장바구니 예외")
    void createOrderFromCart_EmptyCart_ThrowsException() {
        // Given
        given(cartService.getCart(customerId)).willReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrderFromCart(customerId, createOrderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("장바구니가 비어있습니다.");

        verify(cartService).getCart(customerId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("결제 완료 처리 - 성공")
    void completePayment_Success() {
        // Given
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderStatusCodeRepository.findByCode("PAID")).willReturn(Optional.of(paidStatus));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        // When
        orderService.completePayment(orderId, paymentId);

        // Then
        verify(orderRepository).findById(orderId);
        verify(orderStatusCodeRepository).findByCode("PAID");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("총 금액 계산 - 정확한 계산")
    void calculateTotalAmount_CorrectCalculation() {
        // Given
        List<OrderMenu> orderMenuList = Arrays.asList(
                OrderMenu.builder().price(8000).quantity(2).build(),  // 16,000
                OrderMenu.builder().price(7000).quantity(1).build(),  // 7,000
                OrderMenu.builder().price(5000).quantity(3).build()   // 15,000
        );

        // When
        Integer totalAmount = orderService.calculateTotalAmount(orderMenuList);

        // Then
        assertThat(totalAmount).isEqualTo(38000);
    }
}
