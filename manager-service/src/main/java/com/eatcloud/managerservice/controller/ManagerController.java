package com.eatcloud.managerservice.controller;

import com.eatcloud.managerservice.dto.UserDto;
import com.eatcloud.managerservice.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manager")
public class ManagerController {
    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("/search")
    public ResponseEntity<UserDto> searchByEmail(@RequestParam String email) {
        UserDto userDto = managerService.findByEmail(email);
        return ResponseEntity.ok(userDto);
    }
}
