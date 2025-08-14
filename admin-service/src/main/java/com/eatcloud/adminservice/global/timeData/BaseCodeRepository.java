package com.eatcloud.adminservice.global.timeData;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseCodeRepository<T extends BaseTimeEntity, ID> extends BaseTimeRepository<T, ID> {

	// Fetch Join으로 N+1 방지
	@Query("SELECT e FROM #{#entityName} e JOIN FETCH e.timeData WHERE e.timeData.deletedAt IS NULL AND e.isActive = true")
	@Override
	List<T> findAll();

	@Query("SELECT e FROM #{#entityName} e WHERE e.timeData.deletedAt IS NULL AND e.isActive = true AND e.id = ?1")
	@Override
	Optional<T> findById(ID id);

	// 활성 상태 코드만 조회 (정렬 포함)
	@Query("SELECT e FROM #{#entityName} e WHERE e.timeData.deletedAt IS NULL AND e.isActive = true ORDER BY e.sortOrder")
	List<T> findAllActiveSorted();
}