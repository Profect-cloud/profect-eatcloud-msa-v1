package com.eatcloud.autotime.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.eatcloud.autotime.BaseTimeEntity;

/** 물리 삭제 금지. 항상 softDelete* 사용. */
@NoRepositoryBean
public interface SoftDeleteRepository<T extends BaseTimeEntity, ID extends Serializable>
	extends JpaRepository<T, ID> {

	void softDelete(T entity, String actor);
	void softDeleteById(ID id, String actor);

	// 벌크 UPDATE로 deleted*/ 세팅 (리스너 미탐)
	int softDeleteAllByIds(Collection<ID> ids, String actor);

	// 복구
	int restoreAllByIds(Collection<ID> ids);

}
