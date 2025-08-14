package com.eatcloud.adminservice.domain.admin.service;

import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationDetailDto;
import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationSummaryDto;
import com.eatcloud.adminservice.domain.admin.entity.ManagerStoreApplication;
import com.eatcloud.adminservice.domain.admin.exception.AdminErrorCode;
import com.eatcloud.adminservice.domain.admin.exception.AdminException;
import com.eatcloud.adminservice.domain.admin.repository.ManagerStoreApplicationRepository;
import com.eatcloud.adminservice.domain.globalCategory.repository.StoreCategoryRepository;
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
						.appliedAt(app.getTimeData().getCreatedAt())
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
				.appliedAt(app.getTimeData().getCreatedAt())
				.updatedAt(app.getTimeData().getUpdatedAt())
				.build();
	}

	@Transactional
	public void approveApplication(UUID adminId, UUID applicationId) {
		var app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));
		if (!"PENDING".equals(app.getStatus())) {
			throw new AdminException(AdminErrorCode.APPLICATION_ALREADY_PROCESSED);
		}

		// 1) 매니저 계정 보장(없으면 생성, 있으면 업데이트) → managerId 확보

		UUID managerId = managerAdminPort.upsert(new ManagerUpsertCommand(
				app.getManagerId(),              // 보통 null, 있으면 전달
				app.getManagerEmail(),
				app.getManagerName(),
				app.getManagerPhoneNumber()
				// ❗비밀번호는 admin에서 다루지 않는 걸 권장(초대/임시비번은 auth가 처리)
		));
		app.setManagerId(managerId);         // 추적용(선택)

		// 2) 스토어 생성(멱등: applicationId)
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


	@Transactional
	public void rejectApplication(UUID adminId, UUID applicationId) {
		var app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));

		if (!"PENDING".equals(app.getStatus())) {
			throw new AdminException(AdminErrorCode.APPLICATION_ALREADY_PROCESSED);
		}

		app.setStatus("REJECTED");
		app.setReviewerAdminId(adminId);
		applicationRepository.save(app);
		// (선택) 반려 알림 이벤트 발행 가능
	}
}

