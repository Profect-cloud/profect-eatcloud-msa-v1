package com.eatcloud.authservice.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
	private final int code;
	private final String message;
	private final T data;

	private ApiResponse(ApiResponseStatus status, T data) {
		this.code = status.getHttpStatus().value();
		this.message = status.getDisplay();
		this.data = data;
	}

	// 생성 편의 메서드
	public static <T> ApiResponse<T> of(ApiResponseStatus status, T data) {
		return new ApiResponse<>(status, data);
	}

	public static <T> ApiResponse<T> success(T data) {
		return of(ApiResponseStatus.OK, data);
	}

	public static ApiResponse<Void> success() {
		return of(ApiResponseStatus.OK, null);
	}

	public static <T> ApiResponse<T> created(T data) {
		return of(ApiResponseStatus.CREATED, data);
	}
}
