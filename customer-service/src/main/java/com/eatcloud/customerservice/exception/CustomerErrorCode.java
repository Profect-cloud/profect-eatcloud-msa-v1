package com.eatcloud.customerservice.exception;

public enum CustomerErrorCode {
	CUSTOMER_NOT_FOUND("CUSTOMER_001", "해당 고객을 찾을 수 없습니다"),
	INVALID_CUSTOMER_ID("CUSTOMER_002", "유효하지 않은 고객 ID입니다"),
	EMAIL_ALREADY_EXISTS("CUSTOMER_003", "이미 사용 중인 이메일입니다"),
	NICKNAME_ALREADY_EXISTS("CUSTOMER_004", "이미 사용 중인 닉네임입니다"),
	WITHDRAWAL_REASON_REQUIRED("CUSTOMER_005", "탈퇴 사유를 입력해주세요"),
	INVALID_EMAIL_FORMAT("CUSTOMER_006", "올바른 이메일 형식이 아닙니다"),
	INVALID_PHONE_FORMAT("CUSTOMER_007", "올바른 전화번호 형식이 아닙니다"),
	INVALID_UPDATE_REQUEST("CUSTOMER_008", "잘못된 수정 요청입니다"),
	CUSTOMER_ALREADY_WITHDRAWN("CUSTOMER_009", "이미 탈퇴한 고객입니다"),
	ADDRESS_NOT_FOUND("CUSTOMER_010", "해당 배송지를 찾을 수 없습니다"),
	CART_NOT_FOUND("CUSTOMER_011","장바구니를 찾을 수 없습니다"),
	EMPTY_CART("CUSTOMER_012", "장바구니가 비어 있습니다"),
	INVALID_CART_ITEM_REQUEST("CUSTOMER_013", "잘못된 장바구니 요청입니다"),
	CART_ITEM_NOT_FOUND("CUSTOMER_014", "해당 메뉴가 장바구니에 없습니다"),
	CART_STORE_MISMATCH("CUSTOMER_015", "다른 가게의 메뉴는 장바구니에 추가할 수 없습니다. 기존 장바구니를 비운 후 다시 시도해주세요"),
	INVALID_ORDER_TYPE("CUSTOMER_013", "유효하지 않은 주문 타입 코드입니다");


	private final String code;
	private final String message;

	CustomerErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}