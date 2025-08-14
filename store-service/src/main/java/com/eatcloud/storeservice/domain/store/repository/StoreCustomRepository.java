package com.eatcloud.storeservice.domain.store.repository;


import com.eatcloud.storeservice.domain.store.dto.StoreSearchResponseDto;

import java.util.List;
import java.util.UUID;

public interface StoreCustomRepository {
    List<StoreSearchResponseDto> findStoresByCategoryWithinDistance(
            UUID categoryId, double userLat, double userLon, double distanceKm
    );

    List<StoreSearchResponseDto> findStoresByMenuCategoryWithinDistance(
            String menuCategoryCode, double userLat, double userLon, double distanceKm);
}

