package com.eatcloud.orderservice.entity;

public enum OrderStatus {
    PENDING,        // 주문 대기
    CONFIRMED,      // 주문 확인
    PREPARING,      // 준비 중
    READY,          // 준비 완료
    DELIVERING,     // 배달 중
    DELIVERED,      // 배달 완료
    PAID,           // 결제 완료
    FAILED,         // 결제 실패
    CANCELLED,      // 주문 취소
    REFUNDED        // 환불 완료
} 