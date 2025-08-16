package com.eatcloud.customerservice.dto;

import lombok.Builder;
import lombok.Setter;

import java.util.UUID;

@Setter
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String password;
    private String name;
    private String role;
}
