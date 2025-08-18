package com.eatcloud.adminservice.common;

import com.eatcloud.adminservice.domain.admin.exception.AdminErrorCode;
import com.eatcloud.adminservice.domain.admin.exception.AdminException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class})
	public ResponseEntity<String> handleBadRequest(Exception e) {
		return ResponseEntity
			.badRequest()
			.body(e.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
		return ResponseEntity
			.status(HttpStatus.FORBIDDEN)
			.body(e.getMessage());
	}

	@ExceptionHandler(AdminException.class)
	public ResponseEntity<ApiResponse<Void>> handleAdminException(AdminException ex) {
		AdminErrorCode errorCode = ex.getErrorCode();

		ApiResponseStatus status = switch (errorCode) {
			// 리소스를 찾지 못한 경우 ⇒ 404
			case ADMIN_NOT_FOUND, STORE_NOT_FOUND, CATEGORY_NOT_FOUND -> ApiResponseStatus.NOT_FOUND;
			// 잘못된 입력이나 중복 이메일 ⇒ 400
			case EMAIL_ALREADY_EXISTS, INVALID_INPUT -> ApiResponseStatus.BAD_REQUEST;
			// 그 외는 서버 내부 에러로 처리
			default -> ApiResponseStatus.INTERNAL_ERROR;
		};

		ApiResponse<Void> body = ApiResponse.of(status, null);
		return ResponseEntity
			.status(status.getHttpStatus())
			.body(body);
	}

}
