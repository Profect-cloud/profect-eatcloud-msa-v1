package com.eatcloud.autoresponse.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	default String code(){
		return "";
	};                 // e.g. CUSTOMER_001

	default String message(){
		return "";
	}              // 사용자 노출 메시지

	default HttpStatus status() {  // 상태를 안 넣어도 기본 BAD_REQUEST
		return HttpStatus.BAD_REQUEST;
	}
}
