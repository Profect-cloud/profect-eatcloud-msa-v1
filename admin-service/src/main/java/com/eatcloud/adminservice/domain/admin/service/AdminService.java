package com.eatcloud.adminservice.domain.admin.service;

import com.eatcloud.adminservice.domain.admin.dto.ManagerDto;
import com.eatcloud.adminservice.domain.admin.dto.UserDto;
import com.eatcloud.adminservice.domain.admin.exception.AdminErrorCode;
import com.eatcloud.adminservice.domain.admin.exception.AdminException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminService {
	private final CustomerRepository customerRepository;
	private final ManagerRepository managerRepository;
	private final StoreRepository_hong storeRepository;

	@Transactional(readOnly = true)
	public List<UserDto> getAllCustomers() {
		return customerRepository.findAll().stream()
			.map(c -> UserDto.builder()
				.id(c.getId())
				.name(c.getName())
				.nickname(c.getNickname())
				.email(c.getEmail())
				.phoneNumber(c.getPhoneNumber())
				.points(c.getPoints())
				.build()
			)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public UserDto getCustomerByEmail(String email) {
		Customer c = customerRepository.findByEmail(email)
			.orElseThrow(() -> new AdminException(AdminErrorCode.CUSTOMER_NOT_FOUND));

		return UserDto.builder()
			.id(c.getId())
			.name(c.getName())
			.nickname(c.getNickname())
			.email(c.getEmail())
			.phoneNumber(c.getPhoneNumber())
			.points(c.getPoints())
			.build();
	}

	@Transactional
	public void deleteCustomerByEmail(String email) {
		Customer c = customerRepository.findByEmail(email)
			.orElseThrow(() -> new AdminException(AdminErrorCode.CUSTOMER_NOT_FOUND));

		customerRepository.deleteById(c.getId());
	}

	@Transactional(readOnly = true)
	public List<ManagerDto> getAllManagers() {
		return managerRepository.findAll().stream()
			.map(m -> ManagerDto.builder()
				.id(m.getId())
				.name(m.getName())
				.email(m.getEmail())
				.phoneNumber(m.getPhoneNumber())
				.storeId(m.getStore() != null ? m.getStore().getStoreId() : null)
				.build()
			)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ManagerDto getManagerByEmail(String email) {
		Manager m = managerRepository.findByEmail(email)
			.orElseThrow(() -> new AdminException(AdminErrorCode.MANAGER_NOT_FOUND));

		return ManagerDto.builder()
			.id(m.getId())
			.name(m.getName())
			.email(m.getEmail())
			.phoneNumber(m.getPhoneNumber())
			.storeId(m.getStore() != null ? m.getStore().getStoreId() : null)
			.build();
	}

	@Transactional
	public void deleteManagerByEmail(String email) {
		Manager m = managerRepository.findByEmail(email)
			.orElseThrow(() -> new AdminException(AdminErrorCode.MANAGER_NOT_FOUND));

		managerRepository.deleteById(m.getId());
	}

	@Transactional(readOnly = true)
	public List<StoreDto> getStores() {
		return storeRepository.findAll().stream()
			.map(s -> StoreDto.builder()
				.storeId(s.getStoreId())
				.storeName(s.getStoreName())
				.categoryId(s.getStoreCategory().getId())
				.minCost(s.getMinCost())
				.description(s.getDescription())
				.storeLat(s.getStoreLat())
				.storeLon(s.getStoreLon())
				.openStatus(s.getOpenStatus())
				.openTime(s.getOpenTime())
				.closeTime(s.getCloseTime())
				.build()
			)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public StoreDto getStore(UUID storeId) {
		Store s = storeRepository.findById(storeId)
			.orElseThrow(() -> new AdminException(AdminErrorCode.STORE_NOT_FOUND));
		return StoreDto.builder()
			.storeId(s.getStoreId())
			.storeName(s.getStoreName())
			.categoryId(s.getStoreCategory().getId())
			.minCost(s.getMinCost())
			.description(s.getDescription())
			.storeLat(s.getStoreLat())
			.storeLon(s.getStoreLon())
			.openStatus(s.getOpenStatus())
			.openTime(s.getOpenTime())
			.closeTime(s.getCloseTime())
			.build();
	}

	@Transactional
	public void deleteStore(UUID storeId) {
		storeRepository.findById(storeId)
			.orElseThrow(() -> new AdminException(AdminErrorCode.STORE_NOT_FOUND));
		storeRepository.deleteById(storeId);
	}

}