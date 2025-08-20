package com.eatcloud.adminservice.domain.admin.repository;

import com.eatcloud.adminservice.domain.admin.entity.ManagerStoreApplication;
import com.eatcloud.autotime.repository.SoftDeleteRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ManagerStoreApplicationRepository extends SoftDeleteRepository<ManagerStoreApplication, UUID> {
	boolean existsByManagerEmail(String managerEmail);
}