package com.eatcloud.adminservice.domain.admin.service;

import com.eatcloud.adminservice.domain.admin.dto.*;
import com.eatcloud.adminservice.domain.admin.entity.Admin;
import com.eatcloud.adminservice.domain.admin.repository.AdminRepository;
import com.eatcloud.adminservice.ports.CustomerAdminPort;
import com.eatcloud.adminservice.ports.ManagerDirectoryPort;
import com.eatcloud.adminservice.ports.StoreDirectoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final CustomerAdminPort customerPort;
	private final ManagerDirectoryPort managerPort;
	private final StoreDirectoryPort storePort;
	private final AdminRepository adminRepository;

	// ============ Customers ============
	public List<UserDto> getAllCustomers() {
		return customerPort.findAll();
	}

	public UserDto getCustomerByEmail(String email) {
		return customerPort.getByEmail(email);
	}

	public void deleteCustomerByEmail(String email) {
		customerPort.softDeleteByEmail(email);
	}

	// ============ Managers ============
	public List<ManagerDto> getAllManagers() {
		return managerPort.findAll();
	}

	public ManagerDto getManagerByEmail(String email) {
		return managerPort.getByEmail(email);
	}

	public void deleteManagerByEmail(String email) {
		managerPort.softDeleteByEmail(email);
	}

	// ============ Stores ============
	public List<StoreDto> getStores() {
		return storePort.findAll();
	}

	public StoreDto getStore(UUID storeId) {
		return storePort.getById(storeId);
	}

	/**
	 * 지정한 매장을 소프트 삭제(논리 삭제)합니다.
	 *
	 * <p>주어진 매장 ID에 해당하는 매장의 삭제 플래그를 설정하여 실제 데이터는 유지하고 조회에서는 제외되도록 처리합니다.
	 *
	 * @param storeId 소프트 삭제할 매장의 UUID 식별자
	 */
	public void deleteStore(UUID storeId) {
		storePort.softDeleteById(storeId);
	}

	/**
	 * 주어진 이메일로 관리자를 조회하여 로그인에 필요한 UserLoginDto를 반환합니다.
	 *
	 * 지정한 이메일의 Admin 엔티티를 찾지 못하면 RuntimeException("Admin not found")을 던집니다.
	 *
	 * @param email 조회할 관리자 계정의 이메일
	 * @return 관리자 정보(id, email, password, name)와 고정된 역할("admin")이 설정된 UserLoginDto
	 * @throws RuntimeException 관리자를 찾을 수 없을 때 발생
	 */
	public UserLoginDto findByEmail(String email) {
		Admin admin = adminRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Admin not found"));

		return UserLoginDto.builder()
				.id(admin.getId())
				.email(admin.getEmail())
				.password(admin.getPassword())
				.name(admin.getName())
				.role("admin")
				.build();
	}
}
