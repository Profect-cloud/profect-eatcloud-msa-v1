package com.eatcloud.adminservice.domain.admin.repository;

import com.eatcloud.adminservice.domain.admin.entity.ManagerStoreApplication;
import com.eatcloud.adminservice.global.timeData.BaseTimeRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ManagerStoreApplicationRepository extends BaseTimeRepository<ManagerStoreApplication, UUID> {
	boolean existsByManagerEmail(String managerEmail);
}