package com.eatcloud.customerservice.global.timeData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import com.eatcloud.customerservice.SecurityUtil;

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
			String user = SecurityUtil.getCurrentUsername();
			softDeleteByTimeId(entity.getTimeData().getPTimeId(), LocalDateTime.now(), user);
		}
	}

	@Override
	default void deleteById(ID id) {
		Optional<T> entity = findByIdIncludingDeleted(id);
		if (entity.isPresent() && entity.get().getTimeData() != null) {
			String user = SecurityUtil.getCurrentUsername();
			softDeleteByTimeId(entity.get().getTimeData().getPTimeId(), LocalDateTime.now(), user);
		}
	}

	@Override
	default void deleteAll(Iterable<? extends T> entities) {
		String user = SecurityUtil.getCurrentUsername();
		LocalDateTime now = LocalDateTime.now();
		entities.forEach(entity -> {
			if (entity != null && entity.getTimeData() != null) {
				softDeleteByTimeId(entity.getTimeData().getPTimeId(), now, user);
			}
		});
	}

	@Override
	default void deleteAll() {
		String user = SecurityUtil.getCurrentUsername();
		LocalDateTime now = LocalDateTime.now();
		findAll().forEach(entity -> {
			if (entity != null && entity.getTimeData() != null) {
				softDeleteByTimeId(entity.getTimeData().getPTimeId(), now, user);
			}
		});
	}
}