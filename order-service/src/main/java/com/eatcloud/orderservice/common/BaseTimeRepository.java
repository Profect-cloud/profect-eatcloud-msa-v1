package com.eatcloud.orderservice.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseTimeRepository<T extends BaseTimeEntity, ID> extends JpaRepository<T, ID> {

	@Query("SELECT e FROM #{#entityName} e JOIN FETCH e.timeData WHERE e.id = :id")
	Optional<T> findByIdIncludingDeleted(ID id);
	
	@Query("SELECT e FROM #{#entityName} e JOIN FETCH e.timeData")
	List<T> findAllIncludingDeleted();
	
	@Query("SELECT e FROM #{#entityName} e JOIN FETCH e.timeData t WHERE t.deletedAt IS NOT NULL")
	List<T> findDeleted();
	
	@Query("SELECT e FROM #{#entityName} e JOIN FETCH e.timeData t WHERE t.pTimeId = :timeId AND t.deletedAt IS NULL")
	Optional<T> findByTimeIdActive(UUID timeId);
	
	@Query(value = "UPDATE p_time SET deleted_at = :deletedAt, deleted_by = :deletedBy, updated_at = :updatedAt, updated_by = :deletedBy WHERE p_time_id = :timeId", nativeQuery = true)
	void softDeleteByTimeId(UUID timeId, LocalDateTime deletedAt, String deletedBy, LocalDateTime updatedAt);

	@Override
	default void delete(T entity) {
		if (entity != null && entity.getTimeData() != null) {
			String user = "system";
			softDeleteByTimeId(entity.getTimeData().getPTimeId(), LocalDateTime.now(), user, LocalDateTime.now());
		}
	}

	@Override
	default void deleteById(ID id) {
		Optional<T> entity = findByIdIncludingDeleted(id);
		if (entity.isPresent() && entity.get().getTimeData() != null) {
			String user = "system";
			softDeleteByTimeId(entity.get().getTimeData().getPTimeId(), LocalDateTime.now(), user, LocalDateTime.now());
		}
	}

	@Override
	default void deleteAll(Iterable<? extends T> entities) {
		String user = "system";
		LocalDateTime now = LocalDateTime.now();
		entities.forEach(entity -> {
			if (entity != null && entity.getTimeData() != null) {
				softDeleteByTimeId(entity.getTimeData().getPTimeId(), now, user, now);
			}
		});
	}

	@Override
	default void deleteAll() {
		String user = "system";
		LocalDateTime now = LocalDateTime.now();
		findAll().forEach(entity -> {
			if (entity != null && entity.getTimeData() != null) {
				softDeleteByTimeId(entity.getTimeData().getPTimeId(), now, user, now);
			}
		});
	}
}
