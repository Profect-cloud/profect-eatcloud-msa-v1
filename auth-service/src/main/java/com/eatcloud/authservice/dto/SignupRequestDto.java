package com.eatcloud.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private Integer points; // 회원가입 시 포인트
}
