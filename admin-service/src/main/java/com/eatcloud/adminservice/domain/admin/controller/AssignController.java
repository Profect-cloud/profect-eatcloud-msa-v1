package com.eatcloud.adminservice.domain.admin.controller;

import com.eatcloud.adminservice.common.ApiResponse;
import com.eatcloud.adminservice.common.ApiResponseStatus;
import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationRequestDto;
import com.eatcloud.adminservice.domain.admin.dto.ManagerStoreApplicationResponseDto;
import com.eatcloud.adminservice.domain.admin.service.AssignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/unauth")
@Tag(name = "2-4. Unauth API", description = "로그인 없이, Admin에게 신청 요청하는 API")
@AllArgsConstructor
public class AssignController {

	private final AssignService assignService;

	@Operation(summary = "매니저·스토어 신청하기")
	@PostMapping("/manager-apply")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ManagerStoreApplicationResponseDto> apply(
		@RequestBody ManagerStoreApplicationRequestDto request) {

		ManagerStoreApplicationResponseDto resp = assignService.newManagerStoreApply(request);
		return ApiResponse.of(ApiResponseStatus.CREATED, resp);
	}

}
