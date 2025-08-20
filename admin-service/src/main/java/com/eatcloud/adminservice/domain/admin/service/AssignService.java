package com.eatcloud.adminservice.domain.admin.service;

import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationRequestDto;
import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationResponseDto;
import com.eatcloud.adminservice.domain.admin.entity.ManagerStoreApplication;
import com.eatcloud.adminservice.domain.admin.exception.AdminErrorCode;
import com.eatcloud.adminservice.domain.admin.exception.AdminException;
import com.eatcloud.adminservice.domain.admin.repository.ManagerStoreApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignService {

	private final ManagerStoreApplicationRepository managerStoreApplicationRepository;

	@Transactional
	public ManagerStoreApplicationResponseDto newManagerStoreApply(ManagerStoreApplicationRequestDto req) {
		// 1) 중복 검사
		if (managerStoreApplicationRepository.existsByManagerEmail(req.getManagerEmail())) {
			throw new AdminException(AdminErrorCode.APPLICATION_EMAIL_ALREADY_EXISTS);
		}

		ManagerStoreApplication entity = ManagerStoreApplication.builder()
			.managerName(req.getManagerName())
			.managerEmail(req.getManagerEmail())
			.managerPassword(req.getManagerPassword())
			.managerPhoneNumber(req.getManagerPhoneNumber())
			.storeName(req.getStoreName())
			.storeAddress(req.getStoreAddress())
			.storePhoneNumber(req.getStorePhoneNumber())
			.categoryId(req.getCategoryId())
			.description(req.getDescription())
			.status("PENDING")
			.build();

		ManagerStoreApplication saved = managerStoreApplicationRepository.save(entity);

		return ManagerStoreApplicationResponseDto.builder()
			.applicationId(saved.getApplicationId())
			.status(saved.getStatus())
			.createdAt(saved.getCreatedAt())
			.build();
	}

}