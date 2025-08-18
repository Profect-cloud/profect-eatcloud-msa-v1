package com.eatcloud.storeservice.domain.store.service;

import com.eatcloud.storeservice.domain.store.dto.StoreSearchByMenuCategoryRequestDto;
import com.eatcloud.storeservice.domain.store.dto.StoreSearchRequestDto;
import com.eatcloud.storeservice.domain.store.dto.StoreSearchResponseDto;
import com.eatcloud.storeservice.domain.store.exception.StoreAccessDeniedException;
import com.eatcloud.storeservice.domain.store.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    @Autowired
    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<StoreSearchResponseDto> searchStoresByCategoryAndDistance(StoreSearchRequestDto condition) {
        return storeRepository.findStoresByCategoryWithinDistance(
                condition.getCategoryId(),
                condition.getUserLat(),
                condition.getUserLon(),
                condition.getDistanceKm()
        );
    }

    public List<StoreSearchResponseDto> searchStoresByMenuCategory(StoreSearchByMenuCategoryRequestDto condition) {
        return storeRepository.findStoresByMenuCategoryWithinDistance(
                condition.getCategoryCode(),
                condition.getUserLat(),
                condition.getUserLon(),
                condition.getDistanceKm()
        );
    }

    private void validateManagerStoreAccess(UUID managerId, UUID storeId) {
        if (managerId == null || storeId == null) throw new IllegalArgumentException("ids required");
        boolean ok = storeRepository.existsByStoreIdAndManagerId(storeId, managerId);
        if (!ok) throw new StoreAccessDeniedException(managerId.toString(), storeId.toString());
    }
}
