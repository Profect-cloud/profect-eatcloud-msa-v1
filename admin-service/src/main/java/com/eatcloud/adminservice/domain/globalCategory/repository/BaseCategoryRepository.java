package com.eatcloud.adminservice.domain.globalCategory.repository;

import org.springframework.data.repository.NoRepositoryBean;
import profect.eatcloud.domain.globalCategory.entity.BaseCategory;
import profect.eatcloud.global.timeData.BaseTimeRepository;

@NoRepositoryBean
public interface BaseCategoryRepository<E extends BaseCategory> extends BaseTimeRepository<E, Integer> {

}
