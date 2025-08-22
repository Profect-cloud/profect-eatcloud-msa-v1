package com.eatcloud.managerservice.controller;

import java.util.UUID;

import com.eatcloud.autoresponse.core.ApiResponse;
import com.eatcloud.autoresponse.error.BusinessException;
import com.eatcloud.managerservice.dto.ManagerCreateRequest;
import com.eatcloud.managerservice.dto.ManagerDto;
import com.eatcloud.managerservice.dto.ManagerLoginDto;
import com.eatcloud.managerservice.service.ManagerService;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/managers")
@RequiredArgsConstructor
public class ManagerController {

	private final ManagerService managerService;

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ManagerDto>> get(@PathVariable UUID id) {
		ManagerDto dto = managerService.getOrThrow(id); // 못 찾으면 BusinessException 던지기(아래 4번)
		return ResponseEntity.ok(ApiResponse.ok("조회 성공", dto));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ManagerDto>> create(@RequestBody ManagerCreateRequest req) {
		ManagerDto created = managerService.create(req);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.created(created)); // 201 + 기본 메시지("생성이 완료되었습니다.")
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> remove(@PathVariable UUID id) {
		managerService.remove(id);
		return ResponseEntity.ok(ApiResponse.success());
	}

	@GetMapping("/search")
	public ResponseEntity<ManagerLoginDto> searchByEmail(@RequestParam String email) {
		ManagerLoginDto managerLoginDto = managerService.findByEmail(email);
		return ResponseEntity.ok(managerLoginDto);
	}
}

