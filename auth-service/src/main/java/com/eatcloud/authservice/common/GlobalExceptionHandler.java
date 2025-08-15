package com.eatcloud.authservice.common;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleInternalError(Exception e) {
		return ResponseEntity
			.internalServerError()
			.body(e.getMessage());
	}
}
