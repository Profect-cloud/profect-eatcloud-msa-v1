package com.eatcloud.storeservice.domain.menuai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eatcloud.storeservice.domain.menuai.entity.MenuVector;

public interface MenuVectorRepository extends JpaRepository<MenuVector, Long> {

	// 메뉴 이름으로 벡터 조회
	Optional<MenuVector> findByMenuName(String menuName);

	// 모든 벡터 조회 (유사도 계산용)
	@Query("SELECT mv FROM MenuVector mv")
	List<MenuVector> findAllVectors();

	// 메뉴 이름으로 벡터 삭제
	void deleteByMenuName(String menuName);
}
