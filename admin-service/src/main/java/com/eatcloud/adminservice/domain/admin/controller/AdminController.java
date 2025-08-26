package com.eatcloud.adminservice.domain.admin.controller;

import com.eatcloud.adminservice.domain.admin.dto.*;
import com.eatcloud.adminservice.domain.admin.message.ResponseMessage;
import com.eatcloud.adminservice.domain.admin.service.AdminService;
import com.eatcloud.autoresponse.core.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "2-1. Admin API", description = "관리자만 사용하는 API")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class AdminController {

	private final AdminService adminService;

	private UUID getAdminUuid(@AuthenticationPrincipal UserDetails userDetails) {
		return UUID.fromString(userDetails.getUsername());
	}

	@Operation(summary = "1-1. 전체 사용자 목록 조회")
	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<List<UserDto>> getAllCustomers() {
		List<UserDto> users = adminService.getAllCustomers();
		return ApiResponse.success(users);
	}

	@Operation(summary = "1-2. 이메일로 Customer 조회")
	@GetMapping(value = "/users/search", params = "email")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<UserDto> getCustomerByEmail(@RequestParam String email) {
		UserDto customer = adminService.getCustomerByEmail(email);
		return ApiResponse.success(customer);
	}

	@Operation(summary = "1-3. 이메일로 고객 밴(논리 삭제)")
	@DeleteMapping(value = "/customers", params = "email")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ResponseMessage> deleteCustomerByEmail(@RequestParam String email) {
		adminService.deleteCustomerByEmail(email);
		return ApiResponse.success(ResponseMessage.CUSTOMER_BAN_SUCCESS);
	}

	@Operation(summary = "2-1. 전체 매니저 목록 조회")
	@GetMapping("/managers")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<List<ManagerDto>> getAllManagers() {
		List<ManagerDto> list = adminService.getAllManagers();
		return ApiResponse.success(list);
	}

	@Operation(summary = "2-2. 이메일로 매니저 조회")
	@GetMapping(value = "/managers/search", params = "email")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ManagerDto> getManagerByEmail(@RequestParam String email) {
		ManagerDto m = adminService.getManagerByEmail(email);
		return ApiResponse.success(m);
	}

	@Operation(summary = "2-3. 이메일로 매니저 밴(논리 삭제)")
	@DeleteMapping(value = "/managers", params = "email")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ResponseMessage> deleteManagerByEmail(@RequestParam String email) {
		adminService.deleteManagerByEmail(email);
		return ApiResponse.success(ResponseMessage.MANAGER_BAN_SUCCESS);
	}

	@Operation(summary = "3-1. 가게 목록 조회")
	@GetMapping("/stores")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<List<StoreDto>> getStores() {
		List<StoreDto> stores = adminService.getStores();
		return ApiResponse.success(stores);
	}

	@Operation(summary = "3-2. 가게 상세 조회")
	@GetMapping("/stores/{storeId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<StoreDto> getStore(@PathVariable UUID storeId) {
		StoreDto store = adminService.getStore(storeId);
		return ApiResponse.success(store);
	}

	/**
	 * 지정한 가게를 삭제하고 성공 응답을 반환합니다.
	 *
	 * <p>요청한 가게 ID(storeId)에 해당하는 가게를 삭제한 뒤, 삭제 성공 메시지를 담은 ApiResponse를 반환합니다.</p>
	 *
	 * @param storeId 삭제할 가게의 UUID
	 * @return 삭제 성공 메시지를 포함한 ApiResponse (HTTP 200)
	 */
	@Operation(summary = "3-3. 가게 삭제")
	@DeleteMapping("/stores/{storeId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ResponseMessage> deleteStore(@PathVariable UUID storeId) {
		adminService.deleteStore(storeId);
		return ApiResponse.success(ResponseMessage.STORE_DELETE_SUCCESS);
	}

	/**
	 * 이메일을 기준으로 사용자의 로그인 정보를 조회하여 HTTP 200 응답으로 반환합니다.
	 *
	 * @param email 조회할 사용자의 이메일 주소
	 * @return 조회된 사용자 로그인 정보(UserLoginDto)를 포함한 HTTP 200 응답
	 */
	@GetMapping("/search")
	public ResponseEntity<UserLoginDto> searchByEmail(@RequestParam String email) {
		UserLoginDto userLoginDto = adminService.findByEmail(email);
		return ResponseEntity.ok(userLoginDto);
	}

}
