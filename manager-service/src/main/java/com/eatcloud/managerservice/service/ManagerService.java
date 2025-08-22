package com.eatcloud.managerservice.service;

import java.util.Collection;
import java.util.UUID;

import com.eatcloud.managerservice.dto.ManagerLoginDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatcloud.managerservice.dto.ManagerCreateRequest;
import com.eatcloud.managerservice.dto.ManagerDto;
import com.eatcloud.managerservice.entity.Manager;
import com.eatcloud.managerservice.error.ManagerErrorCode;
import com.eatcloud.managerservice.repository.ManagerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {
	private final ManagerRepository managerRepository;

	@Transactional
	public void remove(UUID id) { // actor는 보통 로그인 유저 id
		managerRepository.softDeleteById(id, "김xx");
	}

	@Transactional
	public int bulkRemove(Collection<UUID> ids, String actor) {
		return managerRepository.softDeleteAllByIds(ids, actor); // 벌크 UPDATE (리스너 미탐)
	}

	@Transactional
	public int restore(Collection<UUID> ids) {
		return managerRepository.restoreAllByIds(ids); // 복구
	}

	public ManagerDto create(ManagerCreateRequest req) {
		return null;
	}

	@Transactional(readOnly = true)
	public ManagerDto getOrThrow(UUID id) {
		Manager e = managerRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new com.eatcloud.autoresponse.error.BusinessException(
				ManagerErrorCode.MANAGER_NOT_FOUND));
		return ManagerDto.from(e); // 또는 Mapper 사용
	}

	public ManagerLoginDto findByEmail(String email) {
		Manager manager = managerRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Manager not found"));

		return ManagerLoginDto.builder()
				.id(manager.getId())
				.email(manager.getEmail())
				.password(manager.getPassword())
				.name(manager.getName())
				.role("manager")
				.build();
	}
}

