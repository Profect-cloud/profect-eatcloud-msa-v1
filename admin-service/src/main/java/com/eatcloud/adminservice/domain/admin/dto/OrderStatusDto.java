package com.eatcloud.adminservice.domain.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusDto {
	private String status;
}