package com.eatcloud.adminservice.global.timeData;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BaseTimeRepositoryImpl<T extends BaseTimeEntity, ID>
	extends SimpleJpaRepository<T, ID>
	implements BaseTimeRepository<T, ID> {

	private final EntityManager entityManager;
	private final JpaEntityInformation<T, ID> entityInformation;
	private final String idFieldName;
	private final String entityName;

	public BaseTimeRepositoryImpl(JpaEntityInformation<T, ID> entityInformation,
		EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityManager = entityManager;
		this.entityInformation = entityInformation;
		this.entityName = entityInformation.getEntityName();
		this.idFieldName = findIdFieldName(entityInformation.getJavaType());
	}

	private String findIdFieldName(Class<?> entityClass) {
		Field[] fields = entityClass.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Id.class)) {
				return field.getName();
			}
		}

		Class<?> superClass = entityClass.getSuperclass();
		if (superClass != null && superClass != Object.class) {
			return findIdFieldName(superClass);
		}

		throw new RuntimeException("@Id 어노테이션이 붙은 필드를 찾을 수 없습니다: " + entityClass.getName());
	}

	@Override
	public Optional<T> findById(ID id) {
		String jpql = String.format(
			"SELECT e FROM %s e JOIN FETCH e.timeData t WHERE e.%s = :id AND t.deletedAt IS NULL",
			entityName, idFieldName
		);

		TypedQuery<T> query = entityManager.createQuery(jpql, getDomainClass());
		query.setParameter("id", id);
		return query.getResultList().stream().findFirst();
	}

	@Override
	public List<T> findAll() {
		String jpql = String.format(
			"SELECT e FROM %s e JOIN FETCH e.timeData t WHERE t.deletedAt IS NULL",
			entityName
		);

		TypedQuery<T> query = entityManager.createQuery(jpql, getDomainClass());
		return query.getResultList();
	}

	@Override
	public Optional<T> findByIdIncludingDeleted(ID id) {
		String jpql = String.format(
			"SELECT e FROM %s e JOIN FETCH e.timeData WHERE e.%s = :id",
			entityName, idFieldName
		);

		TypedQuery<T> query = entityManager.createQuery(jpql, getDomainClass());
		query.setParameter("id", id);
		return query.getResultList().stream().findFirst();
	}

	@Override
	public List<T> findAllIncludingDeleted() {
		String jpql = String.format(
			"SELECT e FROM %s e JOIN FETCH e.timeData",
			entityName
		);

		TypedQuery<T> query = entityManager.createQuery(jpql, getDomainClass());
		return query.getResultList();
	}

	@Override
	public List<T> findDeleted() {
		String jpql = String.format(
			"SELECT e FROM %s e JOIN FETCH e.timeData t WHERE t.deletedAt IS NOT NULL",
			entityName
		);

		TypedQuery<T> query = entityManager.createQuery(jpql, getDomainClass());
		return query.getResultList();
	}

	@Override
	public Optional<T> findByTimeIdActive(UUID timeId) {
		String jpql = String.format(
			"SELECT e FROM %s e JOIN FETCH e.timeData t WHERE t.pTimeId = :timeId AND t.deletedAt IS NULL",
			entityName
		);

		TypedQuery<T> query = entityManager.createQuery(jpql, getDomainClass());
		query.setParameter("timeId", timeId);
		return query.getResultList().stream().findFirst();
	}

	@Override
	@Transactional
	public void softDeleteByTimeId(UUID timeId, LocalDateTime deletedAt, String deletedBy) {
		String sql = "UPDATE p_time SET deleted_at = ?, deleted_by = ?, updated_at = ?, updated_by = ? WHERE p_time_id = ?";

		entityManager.createNativeQuery(sql)
			.setParameter(1, deletedAt)
			.setParameter(2, deletedBy)
			.setParameter(3, LocalDateTime.now())
			.setParameter(4, deletedBy)
			.setParameter(5, timeId)
			.executeUpdate();
	}
}
