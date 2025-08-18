package com.eatcloud.adminservice.domain.globalCategory.repository;

import com.eatcloud.adminservice.domain.globalCategory.entity.BaseCategory;
import com.eatcloud.adminservice.global.timeData.BaseTimeRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface BaseCategoryRepository<E extends BaseCategory> extends BaseTimeRepository<E, Integer> {

}
