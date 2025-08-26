package com.eatcloud.storeservice.domain.store.service;

import com.eatcloud.storeservice.domain.store.dto.StoreKeywordSearchRequestDto;
import com.eatcloud.storeservice.domain.store.dto.StoreSearchByMenuCategoryRequestDto;
import com.eatcloud.storeservice.domain.store.dto.StoreSearchRequestDto;
import com.eatcloud.storeservice.domain.store.dto.StoreSearchResponseDto;
import com.eatcloud.storeservice.domain.store.exception.StoreAccessDeniedException;
import com.eatcloud.storeservice.domain.store.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * 키워드와 카테고리 조건으로 매장을 페이징·정렬해서 조회한다.
     *
     * 요청 DTO의 정렬키(`sort`), 정렬방향(`direction`), 페이지(`page`), 페이지크기(`size`)를 기반으로 Pageable을 구성한 뒤
     * repository에 위임하여 결과를 반환한다. `sort`가 "rating"이면 평균 평점 필드(`avgRating`)로 정렬하고,
     * 기본 정렬키는 `createdAt`, 기본 방향은 DESC이다. 동일 정렬값에 대한 결정자(tie-breaker)로 `id`를 DESC로 추가한다.
     *
     * @param req 검색 조건 및 페이징/정렬 정보를 담은 요청 DTO
     * @return 페이징된 StoreSearchResponseDto 결과(Page)
     */
    public Page<StoreSearchResponseDto> searchStoresByKeyword(StoreKeywordSearchRequestDto req) {
        String sortKey = "createdAt";
        if ("rating".equalsIgnoreCase(req.getSort())) {
            // TODO: Store 엔티티에 평균 평점 컬럼명이 다르면 바꿔줘 (예: avgRating)
            sortKey = "avgRating";
        }

        Sort.Direction dir = "ASC".equalsIgnoreCase(req.getDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                req.getPage() == null ? 0 : req.getPage(),
                req.getSize() == null ? 20 : req.getSize(),
                Sort.by(dir, sortKey).and(Sort.by(Sort.Direction.DESC, "id")) // tie-breaker
        );

        return storeRepository.searchByKeywordAndCategory(req, pageable);
    }


}
