package com.eatcloud.adminservice.domain.admin.controller;

import com.eatcloud.adminservice.common.ApiResponse;
import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationDetailDto;
import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationSummaryDto;
import com.eatcloud.adminservice.domain.admin.message.ResponseMessage;
import com.eatcloud.adminservice.domain.admin.service.AdminAssignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "2-3. Admin Assign API", description = "관리자가 신규등록 신청관리 API")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class AdminAssignController {

	private final AdminAssignService adminService;

	private UUID getAdminUuid(@AuthenticationPrincipal UserDetails userDetails) {
		return UUID.fromString(userDetails.getUsername());
	}

	@Operation(summary = "1. Admin: 신청서 목록 조회")
	@GetMapping("/applies")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<List<ManagerStoreApplicationSummaryDto>> listApplications() {
		List<ManagerStoreApplicationSummaryDto> list = adminService.getAllApplications();
		return ApiResponse.success(list);
	}

	@Operation(summary = "2. 신청서 상세 조회")
	@GetMapping("/{applicationId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ManagerStoreApplicationDetailDto> getDetail(@PathVariable UUID applicationId) {
		ManagerStoreApplicationDetailDto dto = adminService.getApplicationDetail(applicationId);
		return ApiResponse.success(dto);
	}

	@Operation(summary = "3. 신청서 승인")
	@PatchMapping("/{applicationId}/approve")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ResponseMessage> approve(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable UUID applicationId) {

		UUID adminUuid = getAdminUuid(userDetails);
		adminService.approveApplication(adminUuid, applicationId);
		return ApiResponse.success(ResponseMessage.APPLICATION_APPROVE_SUCCESS);
	}

	@Operation(summary = "4. 신청서 거절")
	@PatchMapping("/{applicationId}/reject")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ResponseMessage> reject(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable UUID applicationId) {

		UUID adminUuid = getAdminUuid(userDetails);
		adminService.rejectApplication(adminUuid, applicationId);
		return ApiResponse.success(ResponseMessage.APPLICATION_REJECT_SUCCESS);
	}
}
