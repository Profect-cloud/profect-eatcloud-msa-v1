package com.eatcloud.storeservice.domain.store.controller;

import com.eatcloud.autoresponse.core.ApiResponse;
import com.eatcloud.storeservice.domain.store.dto.StoreKeywordSearchRequestDto;
import com.eatcloud.storeservice.domain.store.dto.StoreSearchByMenuCategoryRequestDto;
import com.eatcloud.storeservice.domain.store.dto.StoreSearchRequestDto;
import com.eatcloud.storeservice.domain.store.dto.StoreSearchResponseDto;
import com.eatcloud.storeservice.domain.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@AllArgsConstructor

@Tag(name = "5-1. StoreController")
public class StoreController {

	private final StoreService storeService;

	@Operation(summary = "1. 매장 카테고리 별 거리기반 매장 조회")
	@GetMapping("/search/category")
	public ApiResponse<List<StoreSearchResponseDto>> searchStoresByCategoryAndDistance(
		@ModelAttribute StoreSearchRequestDto condition
	) {
		List<StoreSearchResponseDto> stores = storeService.searchStoresByCategoryAndDistance(condition);
		return ApiResponse.success(stores);
	}

	@Operation(summary = "2. 메뉴 카테고리 별 거리 기반 매장 검색")
	@GetMapping("/search/menu-category")
	public ApiResponse<List<StoreSearchResponseDto>> searchStoresByMenuCategoryAndDistance(
		@ModelAttribute StoreSearchByMenuCategoryRequestDto condition
	) {
		List<StoreSearchResponseDto> stores = storeService.searchStoresByMenuCategory(condition);
		return ApiResponse.success(stores);
	}

	/**
	 * 키워드와 카테고리 조건으로 매장을 조회하며 페이지네이션과 정렬을 지원한다.
	 *
	 * 상세: 요청 DTO에 포함된 키워드·카테고리 조건과 페이지/사이즈/정렬 정보를 기반으로 매장 검색 결과의 페이지를 반환한다.
	 *
	 * @param req 검색 키워드, 카테고리 조건 및 페이지네이션(페이지 번호, 페이지 크기)과 정렬 옵션을 포함한 요청 DTO
	 * @return 검색된 매장 정보를 담은 페이지를 ApiResponse로 래핑하여 반환한다 (Page<StoreSearchResponseDto>)
	 */
	@Operation(summary = "3. 키워드 + 카테고리 + 페이지네이션 + 정렬")
	@GetMapping("/search")
	public ApiResponse<Page<StoreSearchResponseDto>> searchByKeyword(
			@ModelAttribute StoreKeywordSearchRequestDto req
	) {
		return ApiResponse.success(storeService.searchStoresByKeyword(req));
	}

//	@Operation(summary = "0. 전체 매장 조회(페이지네이션)")
//	@GetMapping
//	public ApiResponse<Page<StoreSearchResponseDto>> listStores(
//			@RequestParam(defaultValue = "0") int page,
//			@RequestParam(defaultValue = "20") int size,
//			@RequestParam(defaultValue = "createdAt,desc") String sort
//	) {
//		// sort 파싱: "field,direction"
//		String[] parts = sort.split(",", 2);
//		String sortField = parts[0];
//		String direction = (parts.length > 1 ? parts[1] : "desc");
//
//		if (size > 100) size = 100; // 과도한 조회 방지 (권장)
//
//		Page<StoreSearchResponseDto> result =
//				storeService.listStores(page, size, sortField, direction);
//
//		return ApiResponse.success(result);
//	}

	// 키워드 검색
	// 필터 정렬
}