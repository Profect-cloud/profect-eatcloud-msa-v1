package com.eatcloud.adminservice.domain.admin.service;

import com.eatcloud.adminservice.domain.admin.dto.CategoryDto;
import com.eatcloud.adminservice.domain.admin.exception.AdminErrorCode;
import com.eatcloud.adminservice.domain.admin.exception.AdminException;
import com.eatcloud.adminservice.domain.globalCategory.entity.BaseCategory;
import com.eatcloud.adminservice.domain.globalCategory.entity.MenuCategory;
import com.eatcloud.adminservice.domain.globalCategory.entity.StoreCategory;
import com.eatcloud.adminservice.domain.globalCategory.repository.BaseCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class GenericCategoryService {

	/**
	 * key: 카테고리 타입 (예: "store-categories", "menu-categories")
	 * value: 해당 리포지토리 빈
	 */
	private final Map<String, BaseCategoryRepository<? extends BaseCategory>> repoMap;

	public GenericCategoryService(
		Map<String, BaseCategoryRepository<? extends BaseCategory>> repoMap
	) {
		this.repoMap = repoMap;
	}

	private BaseCategoryRepository<BaseCategory> repo(String type) {
		@SuppressWarnings("unchecked")
		var r = (BaseCategoryRepository<BaseCategory>)repoMap.get(type);
		if (r == null) {
			throw new AdminException(AdminErrorCode.INVALID_INPUT);
		}
		return r;
	}

	@Transactional
	public CategoryDto create(String type, CategoryDto dto) {
		BaseCategory entity = newEntity(type);
		entity.setCode(dto.getCode());
		entity.setDisplayName(dto.getDisplayName());
		entity.setSortOrder(dto.getSortOrder());
		entity.setIsActive(dto.getIsActive());
		BaseCategory saved = repo(type).save(entity);
		return toDto(saved);
	}

	@Transactional
	public CategoryDto update(String type, Integer id, CategoryDto dto) {
		var repository = repo(type);
		BaseCategory entity = repository.findById(id)
			.orElseThrow(() -> new AdminException(AdminErrorCode.CATEGORY_NOT_FOUND));
		entity.setCode(dto.getCode());
		entity.setDisplayName(dto.getDisplayName());
		entity.setSortOrder(dto.getSortOrder());
		entity.setIsActive(dto.getIsActive());
		BaseCategory updated = repository.save(entity);
		return toDto(updated);
	}

	@Transactional
	public void delete(String type, Integer id) {
		var repository = repo(type);
		BaseCategory entity = repository.findById(id)
			.orElseThrow(() -> new AdminException(AdminErrorCode.CATEGORY_NOT_FOUND));
		repository.delete(entity);
	}

	public List<CategoryDto> list(String type) {
		return repo(type).findAll()
			.stream()
			.map(this::toDto)
			.toList();
	}

	// ------------------------
	// helper: 새로운 엔티티 인스턴스를 생성
	// ------------------------
	private BaseCategory newEntity(String type) {
		return switch (type) {
			case "store-categories" -> new StoreCategory();
			case "menu-categories" -> new MenuCategory();
			// 다른 카테고리는 여기에 추가
			default -> throw new AdminException(AdminErrorCode.INVALID_INPUT);
		};
	}

	// ------------------------
	// helper: Entity → DTO 변환
	// ------------------------
	private CategoryDto toDto(BaseCategory e) {
		return CategoryDto.builder()
			.id(e.getId())
			.code(e.getCode())
			.displayName(e.getDisplayName())
			.sortOrder(e.getSortOrder())
			.isActive(e.getIsActive())
			.build();
	}
}
