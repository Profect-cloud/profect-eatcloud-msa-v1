package com.eatcloud.adminservice.domain.admin.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
	private UUID id;
	private String name;
	private String nickname;
	private String email;
	private String password;
	private String phoneNumber;
	private Integer points;
}