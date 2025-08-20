package com.eatcloud.storeservice.domain.manager.service;

import com.eatcloud.storeservice.domain.menu.dto.MenuRequestDto;
import com.eatcloud.storeservice.domain.menu.entity.Menu;
import com.eatcloud.storeservice.domain.menu.exception.MenuErrorCode;
import com.eatcloud.storeservice.domain.menu.exception.MenuException;
import com.eatcloud.storeservice.domain.menu.repository.MenuRepository;
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

@Service
public class ManagerService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    @Autowired
    public ManagerService(MenuRepository menuRepository, StoreRepository storeRepository) {
        this.menuRepository = menuRepository;
        this.storeRepository = storeRepository;
    }

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

        return menuRepository.save(menu);
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

        return menuRepository.save(menu);
    }


    @Transactional
    public void deleteMenu(UUID menuId) {
        menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

        menuRepository.softDeleteById(menuId,"매니저");
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
