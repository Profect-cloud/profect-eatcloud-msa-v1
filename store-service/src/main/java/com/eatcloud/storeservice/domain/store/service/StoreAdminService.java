package com.eatcloud.storeservice.domain.store.service;

import com.eatcloud.storeservice.domain.store.entity.Store;
import com.eatcloud.storeservice.domain.store.exception.StoreErrorCode;
import com.eatcloud.storeservice.domain.store.exception.StoreException;
import com.eatcloud.storeservice.domain.store.repository.StoreRepository;
import com.eatcloud.storeservice.external.admin.AdminCategoryPort;
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
    private final AdminCategoryPort adminCategoryPort; // ✅ 변경
    /**
     * 멱등: applicationKey가 있으면 같은 키로 이미 생성된 스토어를 반환
     */
    public UUID createStore(CreateStoreCommand cmd, UUID applicationKey) {
        if (applicationKey != null) {
            return storeRepository.findByApplicationId(applicationKey)
                    .map(Store::getStoreId)
                    .orElse(null);
        }

        Integer catId = cmd.getStoreCategoryId();


        Store store = Store.builder()
                .storeName(cmd.getStoreName())
                .storeAddress(cmd.getStoreAddress())
                .phoneNumber(cmd.getStorePhoneNumber())
                .storeCategoryId(catId)     // ✅ 정수 ID 저장
                .managerId(cmd.getManagerId())
                .minCost(0)
                .description(cmd.getDescription())
                .openStatus(false)
                .openTime(LocalTime.of(0, 0))
                .closeTime(LocalTime.of(0, 0))
                .applicationId(applicationKey)
                .build();

        try {
            storeRepository.save(store);
            return store.getStoreId();
        } catch (DataIntegrityViolationException dup) {
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
