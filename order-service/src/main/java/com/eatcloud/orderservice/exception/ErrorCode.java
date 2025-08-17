package com.eatcloud.orderservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Cart 관련 에러
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."),
    CART_STORE_MISMATCH(HttpStatus.BAD_REQUEST, "다른 매장의 상품은 함께 담을 수 없습니다."),
    INVALID_CART_ITEM_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 장바구니 요청입니다."),
    INVALID_UPDATE_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 수정 요청입니다."),
    INVALID_CUSTOMER_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 고객 ID입니다."),
    
    // Order 관련 에러
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태입니다."),
    ORDER_PROCESSING(HttpStatus.CONFLICT, "주문이 이미 처리 중입니다. 잠시 후 다시 시도해주세요."),
    EMPTY_CART(HttpStatus.BAD_REQUEST, "장바구니가 비어있습니다."),
    
    // 일반 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
