package com.eatcloud.adminservice.domain.category.repository;


import com.eatcloud.adminservice.domain.category.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuCategoryRepository extends BaseCategoryRepository<MenuCategory> {
    boolean existsByMidCategoryId(Integer midCategoryId);
    boolean existsByMidCategoryIdAndName(Integer midCategoryId, String name);
}
