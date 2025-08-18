package com.eatcloud.storeservice.common;

import com.eatcloud.storeservice.domain.manager.exception.ManagerErrorCode;
import com.eatcloud.storeservice.domain.manager.exception.ManagerException;
import com.eatcloud.storeservice.domain.menu.exception.MenuErrorCode;
import com.eatcloud.storeservice.domain.menu.exception.MenuException;
import com.eatcloud.storeservice.domain.store.exception.AiDescriptionException;
import com.eatcloud.storeservice.domain.store.exception.AiErrorCode;
import com.eatcloud.storeservice.domain.store.exception.StoreErrorCode;
import com.eatcloud.storeservice.domain.store.exception.StoreException;
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


	@ExceptionHandler(MenuException.class)
	public ResponseEntity<ApiResponse<Void>> handleMenuException(MenuException ex) {
		MenuErrorCode errorCode = ex.getErrorCode();

		ApiResponseStatus status = switch (errorCode) {
			case MENU_NOT_FOUND ->ApiResponseStatus.NOT_FOUND;

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
			case STORE_NOT_FOUND,
				 CATEGORY_NOT_FOUND -> ApiResponseStatus.NOT_FOUND;

			case STORE_ALREADY_REGISTERED,
				 STORE_APPLICATION_PENDING,
				 STORE_ALREADY_CLOSED -> ApiResponseStatus.CONFLICT;

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
