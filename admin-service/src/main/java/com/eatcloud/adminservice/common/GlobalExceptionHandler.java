package com.eatcloud.adminservice.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import profect.eatcloud.domain.admin.exception.AdminErrorCode;
import profect.eatcloud.domain.admin.exception.AdminException;
import profect.eatcloud.domain.customer.exception.CustomerErrorCode;
import profect.eatcloud.domain.customer.exception.CustomerException;
import profect.eatcloud.domain.manager.exception.ManagerErrorCode;
import profect.eatcloud.domain.manager.exception.ManagerException;
import profect.eatcloud.domain.store.exception.*;

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

	@ExceptionHandler(CustomerException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomerException(CustomerException ex) {
		CustomerErrorCode errorCode = ex.getErrorCode();

		ApiResponseStatus status = switch (errorCode) {
			case CUSTOMER_NOT_FOUND -> ApiResponseStatus.NOT_FOUND;
			case EMAIL_ALREADY_EXISTS, NICKNAME_ALREADY_EXISTS -> ApiResponseStatus.CONFLICT;
			case INVALID_CUSTOMER_ID, WITHDRAWAL_REASON_REQUIRED,
				 INVALID_EMAIL_FORMAT, INVALID_PHONE_FORMAT,
				 INVALID_UPDATE_REQUEST, CUSTOMER_ALREADY_WITHDRAWN -> ApiResponseStatus.BAD_REQUEST;
			default -> ApiResponseStatus.INTERNAL_ERROR;
		};

		ApiResponse<Void> response = ApiResponse.of(status, null);
		return ResponseEntity.status(status.getHttpStatus()).body(response);
	}

	@ExceptionHandler(MenuException.class)
	public ResponseEntity<ApiResponse<Void>> handleMenuException(MenuException ex) {
		MenuErrorCode errorCode = ex.getErrorCode();

		ApiResponseStatus status = switch (errorCode) {
			case MENU_NOT_FOUND -> ApiResponseStatus.NOT_FOUND;

			case MENU_STORE_MISMATCH,
				 INVALID_MENU_REQUEST,
				 MENU_NAME_REQUIRED,
				 INVALID_MENU_PRICE,
				 DUPLICATE_MENU_NUM -> ApiResponseStatus.BAD_REQUEST;
		};

		return ResponseEntity.status(status.getHttpStatus())
				.body(ApiResponse.of(status, null));
	}


	@ExceptionHandler(StoreException.class)
	public ResponseEntity<ApiResponse<Void>> handleStoreException(StoreException ex) {
		StoreErrorCode errorCode = ex.getErrorCode();

		ApiResponseStatus status = switch (errorCode) {
			case STORE_NOT_FOUND -> ApiResponseStatus.NOT_FOUND;
			case STORE_ALREADY_REGISTERED, STORE_APPLICATION_PENDING, STORE_ALREADY_CLOSED -> ApiResponseStatus.BAD_REQUEST;
			case NOT_AUTHORIZED -> ApiResponseStatus.FORBIDDEN;
		};

		return ResponseEntity.status(status.getHttpStatus())
				.body(ApiResponse.of(status, null));
	}

	@ExceptionHandler(ManagerException.class)
	public ResponseEntity<ApiResponse<Void>> handleManagerException(ManagerException ex) {
		ManagerErrorCode errorCode = ex.getErrorCode();

		ApiResponseStatus status = switch (errorCode) {
			case MANAGER_NOT_FOUND -> ApiResponseStatus.NOT_FOUND;
			case DUPLICATE_APPLICATION -> ApiResponseStatus.BAD_REQUEST;
			case NO_PERMISSION -> ApiResponseStatus.FORBIDDEN;
		};

		return ResponseEntity.status(status.getHttpStatus())
				.body(ApiResponse.of(status, null));
	}

	@ExceptionHandler(AiDescriptionException.class)
	public ResponseEntity<ApiResponse<Void>> handleAiDescriptionException(AiDescriptionException ex) {
		AiErrorCode errorCode = ex.getErrorCode();
		ApiResponseStatus status = ApiResponseStatus.INTERNAL_ERROR;

		return ResponseEntity
				.status(status.getHttpStatus())
				.body(ApiResponse.of(status, null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleInternalError(Exception e) {
		return ResponseEntity
			.internalServerError()
			.body(e.getMessage());
	}
}
