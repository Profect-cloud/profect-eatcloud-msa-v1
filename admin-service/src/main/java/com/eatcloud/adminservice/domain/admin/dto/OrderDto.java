package com.eatcloud.adminservice.domain.admin.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
	private UUID orderId;
	private String orderNumber;
	private UUID customerId;
	private UUID storeId;
	private UUID paymentId;
	private String orderStatus;
	private String orderType;
	private String orderMenuList;  // JSON 형태
}
