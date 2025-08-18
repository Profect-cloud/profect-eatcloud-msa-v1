package com.eatcloud.storeservice.internal.controller;

// com.eatcloud.storeservice.api.internal.StoreAdminController


import com.eatcloud.storeservice.domain.store.service.StoreAdminService;

import com.eatcloud.storeservice.internal.dto.CloseStoreCommand;
import com.eatcloud.storeservice.internal.dto.CreateStoreCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/stores")
public class StoreAdminController {

    private final StoreAdminService storeAdminService;

    /**
     * 가게 생성 (멱등: applicationId 또는 Idempotency-Key 헤더)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID create(@RequestBody CreateStoreCommand cmd,
                       @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // 헤더가 오면 우선 사용, 없으면 cmd.applicationId 사용
        UUID key = idempotencyKey != null ? UUID.fromString(idempotencyKey) : cmd.getApplicationId();
        return storeAdminService.createStore(cmd, key);
    }

    /**
     * 가게 폐업(운영상 닫기) – openStatus=false
     */
    @PostMapping("/{storeId}:close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void close(@PathVariable UUID storeId,
                      @RequestBody(required = false) CloseStoreCommand cmd) {
        storeAdminService.closeStore(storeId, cmd);
    }
}

