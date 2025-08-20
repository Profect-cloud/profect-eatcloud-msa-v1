package com.eatcloud.adminservice.domain.admin.service;

import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationDetailDto;
import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationSummaryDto;
import com.eatcloud.adminservice.domain.admin.entity.ManagerStoreApplication;
import com.eatcloud.adminservice.domain.admin.exception.AdminErrorCode;
import com.eatcloud.adminservice.domain.admin.exception.AdminException;
import com.eatcloud.adminservice.domain.admin.repository.ManagerStoreApplicationRepository;
import com.eatcloud.adminservice.domain.category.repository.StoreCategoryRepository;
import com.eatcloud.adminservice.ports.CreateStoreCommand;
import com.eatcloud.adminservice.ports.ManagerAdminPort;
import com.eatcloud.adminservice.ports.ManagerUpsertCommand;
import com.eatcloud.adminservice.ports.StoreAdminPort;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminAssignService {

	private final ManagerStoreApplicationRepository applicationRepository;
	private final StoreCategoryRepository categoryRepository; // 존재 검증 정도만
	private final StoreAdminPort storeAdminPort;
	private final ManagerAdminPort managerAdminPort;

	@Transactional(readOnly = true)
	public List<ManagerStoreApplicationSummaryDto> getAllApplications() {
		return applicationRepository.findAll().stream()
				.map(app -> ManagerStoreApplicationSummaryDto.builder()
						.applicationId(app.getApplicationId())
						.managerName(app.getManagerName())
						.managerEmail(app.getManagerEmail())
						.storeName(app.getStoreName())
						.status(app.getStatus())
						.appliedAt(app.getCreatedAt())
						.build())
				.toList();
	}

	@Transactional(readOnly = true)
	public ManagerStoreApplicationDetailDto getApplicationDetail(UUID applicationId) {
		var app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));

		return ManagerStoreApplicationDetailDto.builder()
				.applicationId(app.getApplicationId())
				.managerName(app.getManagerName())
				.managerEmail(app.getManagerEmail())
				.managerPhoneNumber(app.getManagerPhoneNumber())
				.storeName(app.getStoreName())
				.storeAddress(app.getStoreAddress())
				.storePhoneNumber(app.getStorePhoneNumber())
				.categoryId(app.getCategoryId())
				.description(app.getDescription())
				.status(app.getStatus())
				.reviewerAdminId(app.getReviewerAdminId())
				.reviewComment(app.getReviewComment())
				.appliedAt(app.getCreatedAt())
				.updatedAt(app.getUpdatedAt())
				.build();
	}

	@Transactional
	public void approveApplication(UUID adminId, UUID applicationId) {
		var app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));

		if (!"PENDING".equals(app.getStatus())) {
			throw new AdminException(AdminErrorCode.APPLICATION_ALREADY_PROCESSED);
		}

		// (선택) 카테고리 존재 검증
		if (app.getCategoryId() != null && !categoryRepository.existsById(app.getCategoryId())) {
			throw new AdminException(AdminErrorCode.CATEGORY_NOT_FOUND);
		}

		// 1) 매니저 계정 보장 → email 기반 upsert, managerId 반환
		UUID managerId = managerAdminPort.upsert(new ManagerUpsertCommand(
				null,                              // 신청서엔 managerId가 없으므로 null
				app.getManagerEmail(),
				app.getManagerName(),
				app.getManagerPhoneNumber()
		));

		// 2) 스토어 생성(멱등키: applicationId)
		storeAdminPort.createStore(new CreateStoreCommand(
				app.getApplicationId(),
				managerId,
				app.getStoreName(),
				app.getStoreAddress(),
				app.getStorePhoneNumber(),
				app.getCategoryId(),
				app.getDescription()
		));

		// 3) 상태 변경
		app.setStatus("APPROVED");
		app.setReviewerAdminId(adminId);
		applicationRepository.save(app);
	}

	// AdminAssignService (또는 AdminService)
	@Transactional
	public void rejectApplication(UUID adminId, UUID applicationId) {
		rejectApplication(adminId, applicationId, null); // 코멘트 없이 거절
	}

	@Transactional
	public void rejectApplication(UUID adminId, UUID applicationId, String comment) {
		var app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));

		if ("REJECTED".equals(app.getStatus())) return;
		if (!"PENDING".equals(app.getStatus())) {
			throw new AdminException(AdminErrorCode.APPLICATION_ALREADY_PROCESSED);
		}

		app.setStatus("REJECTED");
		app.setReviewerAdminId(adminId);
		app.setReviewComment(comment); // null 가능
		applicationRepository.save(app);
	}

}

