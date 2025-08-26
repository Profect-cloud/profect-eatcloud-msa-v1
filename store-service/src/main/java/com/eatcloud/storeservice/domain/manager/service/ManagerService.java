package com.eatcloud.storeservice.domain.manager.service;

import com.eatcloud.storeservice.domain.menu.dto.MenuRequestDto;
import com.eatcloud.storeservice.domain.menu.entity.Menu;
import com.eatcloud.storeservice.domain.menu.exception.MenuErrorCode;
import com.eatcloud.storeservice.domain.menu.exception.MenuException;
import com.eatcloud.storeservice.domain.menu.repository.MenuRepository;
import com.eatcloud.storeservice.domain.menuai.service.TFIDFService;
import com.eatcloud.storeservice.domain.store.dto.StoreRequestDto;
import com.eatcloud.storeservice.domain.store.entity.Store;
import com.eatcloud.storeservice.domain.store.exception.StoreErrorCode;
import com.eatcloud.storeservice.domain.store.exception.StoreException;
import com.eatcloud.storeservice.domain.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ManagerService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final TFIDFService tfidfService;

    @Autowired
    public ManagerService(MenuRepository menuRepository, StoreRepository storeRepository, TFIDFService tfidfService) {
        this.menuRepository = menuRepository;
        this.storeRepository = storeRepository;
        this.tfidfService = tfidfService;
    }

    // 메뉴 생성
    @Transactional
    public Menu createMenu(UUID storeId, MenuRequestDto dto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new MenuException(MenuErrorCode.INVALID_MENU_PRICE);
        }

        if (dto.getMenuName() == null || dto.getMenuName().trim().isEmpty()) {
            throw new MenuException(MenuErrorCode.MENU_NAME_REQUIRED);
        }

        Boolean isAvailable = dto.getIsAvailable();
        if (isAvailable == null) {
            isAvailable = true;
        }

        if (menuRepository.existsByStoreAndMenuNum(store, dto.getMenuNum())) {
            throw new MenuException(MenuErrorCode.DUPLICATE_MENU_NUM);
        }

        Menu menu = Menu.builder()
                .store(store)
                .menuNum(dto.getMenuNum())
                .menuName(dto.getMenuName())
                .menuCategoryCode(dto.getMenuCategoryCode())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .isAvailable(isAvailable)
                .imageUrl(dto.getImageUrl())
                .build();

        Menu savedMenu = menuRepository.save(menu);
        log.info("메뉴 생성 완료: {}", savedMenu.getMenuName());

        // TF-IDF 벡터 생성 및 저장 (동기 처리로 변경)
        try {
            tfidfService.generateAndSaveMenuVector(savedMenu);
            log.info("메뉴 벡터 생성 완료: {}", savedMenu.getMenuName());
        } catch (Exception e) {
            log.error("메뉴 벡터 생성 실패: {}", savedMenu.getMenuName(), e);
        }

        return savedMenu;
    }

    public Menu updateMenu(UUID storeId, UUID menuId, MenuRequestDto dto) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

        if (dto.getMenuName() == null || dto.getMenuName().trim().isEmpty()) {
            throw new MenuException(MenuErrorCode.MENU_NAME_REQUIRED);
        }

        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new MenuException(MenuErrorCode.INVALID_MENU_PRICE);
        }

        if (dto.getMenuNum() != menu.getMenuNum()) {
            boolean exists = menuRepository.existsByStoreAndMenuNum(menu.getStore(), dto.getMenuNum());
            if (exists) {
                throw new MenuException(MenuErrorCode.DUPLICATE_MENU_NUM);
            }
        }

        menu.setMenuNum(dto.getMenuNum());
        menu.setMenuName(dto.getMenuName());
        menu.setMenuCategoryCode(dto.getMenuCategoryCode());
        menu.setPrice(dto.getPrice());
        menu.setDescription(dto.getDescription());
        menu.setIsAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true);
        menu.setImageUrl(dto.getImageUrl());

        Menu updatedMenu = menuRepository.save(menu);
        log.info("메뉴 수정 완료: {}", updatedMenu.getMenuName());

        // 벡터 재생성 (비동기 처리)
        CompletableFuture.runAsync(() -> {
            try {
                tfidfService.generateAndSaveMenuVector(updatedMenu);
            } catch (Exception e) {
                log.error("메뉴 벡터 재생성 실패: {}", updatedMenu.getMenuName(), e);
            }
        });

        return updatedMenu;
    }

    @Transactional
    public void deleteMenu(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

        String menuName = menu.getMenuName();
        menuRepository.softDeleteById(menuId,"매니저");
        log.info("메뉴 삭제 완료: {}", menuName);

        // 벡터도 함께 삭제 (비동기 처리)
        CompletableFuture.runAsync(() -> {
            try {
                tfidfService.deleteMenuVector(menuName);
            } catch (Exception e) {
                log.error("메뉴 벡터 삭제 실패: {}", menuName, e);
            }
        });
    }

    public void updateStore(UUID storeId, StoreRequestDto dto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (dto.getStoreName() != null) store.setStoreName(dto.getStoreName());
        if (dto.getStoreAddress() != null) store.setStoreAddress(dto.getStoreAddress());
        if (dto.getPhoneNumber() != null) store.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getMinCost() != null) store.setMinCost(dto.getMinCost());
        if (dto.getDescription() != null) store.setDescription(dto.getDescription());
        if (dto.getStoreLat() != null) store.setStoreLat(dto.getStoreLat());
        if (dto.getStoreLon() != null) store.setStoreLon(dto.getStoreLon());
        if (dto.getOpenTime() != null) store.setOpenTime(dto.getOpenTime());
        if (dto.getCloseTime() != null) store.setCloseTime(dto.getCloseTime());
    }
}
