package com.eatcloud.storeservice.domain.store.repository;


import com.eatcloud.autotime.repository.SoftDeleteRepository;
import com.eatcloud.storeservice.domain.store.entity.Store;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends SoftDeleteRepository<Store, UUID>, StoreCustomRepository{

    Optional<Store> findById(UUID storeId);

    boolean existsByStoreIdAndManagerId(UUID storeId, UUID managerId);
    Optional<Store> findByApplicationId(UUID applicationId);


}
