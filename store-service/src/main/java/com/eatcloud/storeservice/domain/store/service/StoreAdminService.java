package com.eatcloud.storeservice.domain.store.service;

import com.eatcloud.storeservice.domain.store.entity.Store;
import com.eatcloud.storeservice.domain.store.exception.StoreErrorCode;
import com.eatcloud.storeservice.domain.store.exception.StoreException;
import com.eatcloud.storeservice.domain.store.repository.StoreRepository;
import com.eatcloud.storeservice.globalCategory.entity.StoreCategory;
import com.eatcloud.storeservice.globalCategory.repository.StoreCategoryRepository;
import com.eatcloud.storeservice.internal.dto.CloseStoreCommand;
import com.eatcloud.storeservice.internal.dto.CreateStoreCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreAdminService {

    private final StoreRepository storeRepository;
    private final StoreCategoryRepository categoryRepository;

    /**
     * 멱등: applicationKey가 있으면 같은 키로 이미 생성된 스토어를 반환
     */
    public UUID createStore(CreateStoreCommand cmd, UUID applicationKey) {
        // 1) 멱등 조회
        if (applicationKey != null) {
            Optional<Store> already = storeRepository.findByApplicationId(applicationKey);
            if (already.isPresent()) {
                return already.get().getStoreId();
            }
        }

        // 2) 카테고리 확인 (nullable 허용이면 조건부)
        StoreCategory category = null;
        if (cmd.getCategoryId() != null) {
            category = categoryRepository.findById(cmd.getCategoryId())
                    .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));
        }

        // 3) 엔티티 생성 (초기 미오픈)
        Store store = Store.builder()
                .storeName(cmd.getStoreName())
                .storeAddress(cmd.getStoreAddress())
                .phoneNumber(cmd.getStorePhoneNumber())
                .storeCategory(category)
                .managerId(cmd.getManagerId())
                .minCost(0)
                .description(cmd.getDescription())
                .openStatus(false)
                .openTime(LocalTime.of(0, 0))
                .closeTime(LocalTime.of(0, 0))
                .build();

        store.setApplicationId(applicationKey);

        // 4) 저장 (+동시성 대비)
        try {
            storeRepository.save(store);
            return store.getStoreId();
        } catch (DataIntegrityViolationException dup) {
            // application_id UNIQUE 충돌 케이스 → 기존 것 반환
            return storeRepository.findByApplicationId(applicationKey)
                    .orElseThrow(() -> dup)
                    .getStoreId();
        }
    }

    public void closeStore(UUID storeId, CloseStoreCommand cmd) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (Boolean.FALSE.equals(store.getOpenStatus())) {
            throw new StoreException(StoreErrorCode.STORE_ALREADY_CLOSED);
        }

        // 운영상 폐업: 오픈 상태만 변경
        store.setOpenStatus(false);
        // 필요하면 cmd.getReason()를 별도 감사 테이블에 적재

        storeRepository.save(store);
    }
}
