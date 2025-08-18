package com.eatcloud.adminservice.global.timeData;

import com.eatcloud.adminservice.global.timeData.BaseTimeRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
	basePackages = "com.eatcloud",
	repositoryBaseClass = BaseTimeRepositoryImpl.class
)
public class JpaConfig {
}
