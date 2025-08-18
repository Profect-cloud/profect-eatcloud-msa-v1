package com.eatcloud.storeservice.domain.store.repository;

import com.eatcloud.storeservice.domain.store.dto.StoreSearchResponseDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class StoreCustomRepositoryImpl implements StoreCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<StoreSearchResponseDto> findStoresByCategoryWithinDistance(UUID categoryId, double lat, double lon, double distanceKm) {
        String sql = """
                SELECT 
                    s.store_id,
                    s.store_name,
                    s.store_address,
                    s.store_lat,
                    s.store_lon,
                    s.min_cost,
                    s.open_status
                FROM p_stores s
                WHERE s.category_id = :categoryId
                  AND ST_DWithin(
                        geography(ST_MakePoint(s.store_lon, s.store_lat)),
                        geography(ST_MakePoint(:lon, :lat)),
                        :distanceMeters
                  )
                  AND s.open_status = true
            """;

        List<Object[]> resultList = em.createNativeQuery(sql)
                .setParameter("categoryId", categoryId)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .setParameter("distanceMeters", distanceKm * 1000)
                .getResultList();

        return resultList.stream()
                .map(row -> StoreSearchResponseDto.of(
                        (UUID) row[0],
                        (String) row[1],
                        (String) row[2],
                        (Double) row[3],
                        (Double) row[4],
                        (Integer) row[5],
                        (Boolean) row[6]
                ))
                .toList();
    }

    @Override
    public List<StoreSearchResponseDto> findStoresByMenuCategoryWithinDistance(
            String menuCategoryCode, double userLat, double userLon, double distanceKm) {

        String sql = """
        SELECT 
            s.store_id,
            s.store_name,
            s.store_address,
            s.store_lat,
            s.store_lon,
            s.min_cost,
            s.open_status
        FROM p_stores s
        JOIN p_menus m ON m.store_id = s.store_id
        WHERE ST_DistanceSphere(
            ST_MakePoint(s.store_lon, s.store_lat),
            ST_MakePoint(:userLon, :userLat)
        ) <= (:distanceKm * 1000)
        AND m.menu_category_code = :menuCategoryCode
        AND s.open_status = true
        GROUP BY s.store_id, s.store_name, s.store_address, s.store_lat, s.store_lon, s.min_cost, s.open_status
    """;

        List<Object[]> resultList = em.createNativeQuery(sql)
                .setParameter("userLat", userLat)
                .setParameter("userLon", userLon)
                .setParameter("distanceKm", distanceKm)
                .setParameter("menuCategoryCode", menuCategoryCode)
                .getResultList();

        return resultList.stream()
                .map(row -> StoreSearchResponseDto.of(
                        (UUID) row[0],
                        (String) row[1],
                        (String) row[2],
                        (Double) row[3],
                        (Double) row[4],
                        (Integer) row[5],
                        (Boolean) row[6]
                ))
                .toList();
    }
}
