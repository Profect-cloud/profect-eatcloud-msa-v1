package com.eatcloud.customerservice.repository;

import java.util.Optional;
import java.util.UUID;

import com.eatcloud.autotime.repository.SoftDeleteRepository;
import com.eatcloud.customerservice.entity.Customer;

public interface CustomerRepository extends SoftDeleteRepository<Customer, UUID> {
	Optional<Customer> findByEmail(String email);

	Optional<Customer> findByNameAndTimeData_DeletedAtIsNull(String name);
	Optional<Customer> findByEmailAndTimeData_DeletedAtIsNull(String email);
	boolean existsByNameAndTimeData_DeletedAtIsNull(String name);
	boolean existsByEmailAndTimeData_DeletedAtIsNull(String email);
	boolean existsByNicknameAndTimeData_DeletedAtIsNull(String nickname);
}
