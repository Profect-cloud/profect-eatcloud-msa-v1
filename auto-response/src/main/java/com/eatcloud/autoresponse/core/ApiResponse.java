package com.eatcloud.autoresponse.core;

import com.eatcloud.autoresponse.error.ApiError;
import com.eatcloud.autoresponse.error.ErrorCode;
import com.eatcloud.autoresponse.message.MessageResolvable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 응답 DTO
 * - code : HTTP status value (예: 200, 201, 400 ...)
 * - message : 사용자 메시지
 * - data : 페이로드(성공 시 DTO, 실패 시 ApiError)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.ALWAYS) // data=null도 항상 노출(팀 FE 합의에 맞게 조정 가능)
public class ApiResponse<T> {

	private final int code;
	private final String message;
	private final T data;

	// ===== 성공 계열 =====

	/** 표준 상태/기본 메시지 */
	public static <T> ApiResponse<T> of(final ApiResponseStatus status, final T data) {
		return new ApiResponse<>(status.getHttpStatus().value(), status.getDisplay(), data);
	}

	/** 커스텀 상태/메시지 */
	public static <T> ApiResponse<T> with(final HttpStatus status, final String message, final T data) {
		return new ApiResponse<>(status.value(), message, data);
	}

	/** 200 OK + 기본 메시지 */
	public static <T> ApiResponse<T> success(final T data) {
		return of(ApiResponseStatus.OK, data);
	}

	/** 200 OK + 기본 메시지 (no body) */
	public static ApiResponse<Void> success() {
		return of(ApiResponseStatus.OK, null);
	}

	/** 201 Created + 기본 메시지 */
	public static <T> ApiResponse<T> created(final T data) {
		return of(ApiResponseStatus.CREATED, data);
	}

	/** 200 OK + 도메인 메시지 Enum */
	public static <T> ApiResponse<T> success(final MessageResolvable m, final T data) {
		return with(HttpStatus.OK, m.message(), data);
	}

	/** 201 Created + 도메인 메시지 Enum */
	public static <T> ApiResponse<T> created(final MessageResolvable m, final T data) {
		return with(HttpStatus.CREATED, m.message(), data);
	}

	/** 200 OK + 커스텀 메시지 */
	public static <T> ApiResponse<T> ok(final String message, final T data) {
		return with(HttpStatus.OK, message, data);
	}

	// ===== 오류 계열 =====

	/** 임의 상태/메시지/에러 페이로드 */
	public static ApiResponse<ApiError> error(final HttpStatus status,
		final String message,
		final ApiError error) {
		return new ApiResponse<>(status.value(), message, error);
	}

	/** 400 Bad Request */
	public static ApiResponse<ApiError> badRequest(final String message, final ApiError error) {
		return error(HttpStatus.BAD_REQUEST, message, error);
	}

	/** 400 Bad Request (표준 메시지) */
	public static ApiResponse<ApiError> badRequest(final ApiError error) {
		return badRequest(ApiResponseStatus.BAD_REQUEST.getDisplay(), error);
	}

	/** 404 Not Found */
	public static ApiResponse<ApiError> notFound(final String message) {
		return error(HttpStatus.NOT_FOUND, message, null);
	}

	/** 403 Forbidden */
	public static ApiResponse<ApiError> forbidden(final String message) {
		return error(HttpStatus.FORBIDDEN, message, null);
	}

	/** 401 Unauthorized */
	public static ApiResponse<ApiError> unauthorized(final String message) {
		return error(HttpStatus.UNAUTHORIZED, message, null);
	}

	/** 409 Conflict */
	public static ApiResponse<ApiError> conflict(final String message) {
		return error(HttpStatus.CONFLICT, message, null);
	}

	/** 500 Internal Server Error */
	public static ApiResponse<ApiError> internalError(final String message) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
	}

	// ===== 도메인 ErrorCode 연동 =====

	/** ErrorCode만으로 본문(data=null) */
	public static ApiResponse<Void> from(final ErrorCode ec) {
		return with(ec.status(), ec.message(), null);
	}

	/** ErrorCode + ApiError(필드 오류 등 상세) */
	public static ApiResponse<ApiError> from(final ErrorCode ec, final ApiError error) {
		return error(ec.status(), ec.message(), error);
	}
}
