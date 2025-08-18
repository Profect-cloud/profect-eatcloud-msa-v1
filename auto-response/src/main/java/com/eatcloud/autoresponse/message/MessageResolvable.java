package com.eatcloud.autoresponse.message;

public interface MessageResolvable {
	String message();              // 성공 메시지 Enum에서 사용
	default String code() {
		return null;
	}
}

