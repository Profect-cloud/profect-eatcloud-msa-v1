package com.eatcloud.adminservice.domain.category.repository;

import com.eatcloud.adminservice.domain.category.entity.BaseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseCategoryRepository<T extends BaseCategory> extends JpaRepository<T, Integer> {
}
