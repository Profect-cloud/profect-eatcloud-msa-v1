package com.eatcloud.adminservice.domain.admin.repository;


import com.eatcloud.adminservice.domain.admin.entity.Admin;
import com.eatcloud.adminservice.global.timeData.BaseTimeRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends BaseTimeRepository<Admin, UUID> {
	Optional<Admin> findByEmail(String email);
}