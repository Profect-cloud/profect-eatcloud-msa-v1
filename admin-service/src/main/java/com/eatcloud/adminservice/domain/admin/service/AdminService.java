package com.eatcloud.adminservice.domain.admin.service;

import com.eatcloud.adminservice.domain.admin.dto.ManagerDto;
import com.eatcloud.adminservice.domain.admin.dto.StoreDto;
import com.eatcloud.adminservice.domain.admin.dto.UserDto;
import com.eatcloud.adminservice.ports.CustomerAdminPort;
import com.eatcloud.adminservice.ports.ManagerDirectoryPort;
import com.eatcloud.adminservice.ports.StoreDirectoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final CustomerAdminPort customerPort;
	private final ManagerDirectoryPort managerPort;
	private final StoreDirectoryPort storePort;

	// ============ Customers ============
	public List<UserDto> getAllCustomers() {
		return customerPort.findAll();
	}

	public UserDto getCustomerByEmail(String email) {
		return customerPort.getByEmail(email);
	}

	public void deleteCustomerByEmail(String email) {
		customerPort.deleteByEmail(email);
	}

	// ============ Managers ============
	public List<ManagerDto> getAllManagers() {
		return managerPort.findAll();
	}

	public ManagerDto getManagerByEmail(String email) {
		return managerPort.getByEmail(email);
	}

	public void deleteManagerByEmail(String email) {
		managerPort.deleteByEmail(email);
	}

	// ============ Stores ============
	public List<StoreDto> getStores() {
		return storePort.findAll();
	}

	public StoreDto getStore(UUID storeId) {
		return storePort.getById(storeId);
	}

	public void deleteStore(UUID storeId) {
		storePort.deleteById(storeId);
	}
}
