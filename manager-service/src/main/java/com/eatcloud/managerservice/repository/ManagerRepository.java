package com.eatcloud.managerservice.repository;

import com.eatcloud.managerservice.entity.Manager;
import com.eatcloud.managerservice.global.timeData.BaseTimeRepository;

import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository extends BaseTimeRepository<Manager, UUID> {
	Optional<Manager> findByEmail(String email);
}
