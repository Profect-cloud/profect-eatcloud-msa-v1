package com.eatcloud.autoresponse.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	String code();                 // e.g. CUSTOMER_001
	String message();              // 사용자 노출 메시지
	default HttpStatus status() {  // 상태를 안 넣어도 기본 BAD_REQUEST
		return HttpStatus.BAD_REQUEST;
	}
}
