package com.eatcloud.paymentservice.entity;

public enum PaymentRequestStatus {
    PENDING,        // 대기
    PROCESSING,     // 처리 중
    COMPLETED,      // 완료
    FAILED,         // 실패
    CANCELLED,      // 취소
    TIMEOUT         // 타임아웃
} 