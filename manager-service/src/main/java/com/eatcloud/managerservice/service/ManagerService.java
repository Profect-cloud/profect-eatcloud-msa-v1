package com.eatcloud.managerservice.service;

import com.eatcloud.managerservice.dto.UserDto;
import com.eatcloud.managerservice.entity.Manager;
import com.eatcloud.managerservice.repository.ManagerRepository;
import org.springframework.stereotype.Service;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;

    public ManagerService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    public UserDto findByEmail(String email) {
        Manager manager = managerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        return UserDto.builder()
                .id(manager.getId())
                .email(manager.getEmail())
                .password(manager.getPassword())
                .name(manager.getName())
                .role("manager")
                .build();
    }
}
