package com.eatcloud.storeservice.globalCategory.repository;

import com.eatcloud.storeservice.global.timeData.BaseTimeRepository;
import com.eatcloud.storeservice.globalCategory.entity.BaseCategory;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface BaseCategoryRepository<E extends BaseCategory> extends BaseTimeRepository<E, Integer> {

}
