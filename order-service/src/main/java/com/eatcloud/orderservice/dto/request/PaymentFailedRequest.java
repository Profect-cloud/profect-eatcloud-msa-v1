package com.eatcloud.orderservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailedRequest {
    
    private UUID paymentId;          // 실패한 결제 ID (있는 경우)
    
    @NotBlank(message = "실패 사유는 필수입니다")
    private String failureReason;    // 실패 사유
    
    private String errorCode;        // PG사 에러 코드
    private String paymentMethod;    // 시도된 결제 수단
}
