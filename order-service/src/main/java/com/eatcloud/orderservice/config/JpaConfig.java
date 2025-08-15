package com.eatcloud.orderservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.eatcloud.orderservice.common.BaseTimeRepositoryImpl;

@Configuration
@EnableJpaRepositories(
	basePackages = "com.eatcloud",
	repositoryBaseClass = BaseTimeRepositoryImpl.class
)
public class JpaConfig {
    // JPA 관련 추가 설정이 필요한 경우 여기에 추가
}
