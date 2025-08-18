package com.eatcloud.customerservice.common;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eatcloud.customerservice.exception.CustomerErrorCode;
import com.eatcloud.customerservice.exception.CustomerException;

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

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleInternalError(Exception e) {
		return ResponseEntity
			.internalServerError()
			.body(e.getMessage());
	}
}
