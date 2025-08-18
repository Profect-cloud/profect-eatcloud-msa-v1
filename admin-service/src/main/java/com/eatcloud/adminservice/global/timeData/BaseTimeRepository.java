package com.eatcloud.adminservice.global.timeData;

import com.eatcloud.adminservice.global.timeData.BaseTimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseTimeRepository<T extends BaseTimeEntity, ID> extends JpaRepository<T, ID> {

	Optional<T> findByIdIncludingDeleted(ID id);
	List<T> findAllIncludingDeleted();
	List<T> findDeleted();
	Optional<T> findByTimeIdActive(UUID timeId);
	void softDeleteByTimeId(UUID timeId, LocalDateTime deletedAt, String deletedBy);

	@Override
	default void delete(T entity) {
		if (entity != null && entity.getTimeData() != null) {
			softDeleteByTimeId(entity.getTimeData().getPTimeId(), LocalDateTime.now(), "system");
		}
	}

	@Override
	default void deleteById(ID id) {
		Optional<T> entity = findByIdIncludingDeleted(id);
		if (entity.isPresent() && entity.get().getTimeData() != null) {
			softDeleteByTimeId(entity.get().getTimeData().getPTimeId(), LocalDateTime.now(), "system");
		}
	}

	@Override
	default void deleteAll(Iterable<? extends T> entities) {
		LocalDateTime now = LocalDateTime.now();
		for (T entity : entities) {
			if (entity != null && entity.getTimeData() != null) {
				softDeleteByTimeId(entity.getTimeData().getPTimeId(), now, "system");
			}
		}
	}

	@Override
	default void deleteAll() {
		LocalDateTime now = LocalDateTime.now();
		for (T entity : findAll()) {
			if (entity != null && entity.getTimeData() != null) {
				softDeleteByTimeId(entity.getTimeData().getPTimeId(), now, "system");
			}
		}
	}
}
