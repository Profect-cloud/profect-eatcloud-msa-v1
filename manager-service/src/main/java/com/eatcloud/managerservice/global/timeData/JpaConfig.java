package com.eatcloud.managerservice.global.timeData;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
	basePackages = "com.eatcloud.managerservice",
	repositoryBaseClass = BaseTimeRepositoryImpl.class
)
public class JpaConfig {
}
