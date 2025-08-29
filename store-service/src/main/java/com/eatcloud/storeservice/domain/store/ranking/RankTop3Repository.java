package com.eatcloud.storeservice.domain.store.ranking;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // ★ 반드시 추가
public interface RankTop3Repository extends Repository<Object, String> {
    @Query(value = """
        SELECT
          store_category_id AS storeCategoryId,
          rank              AS rank,
          store_id          AS storeId,
          store_name        AS storeName,
          avg_rating        AS avgRating,
          rating_count      AS ratingCount,
          updated_at        AS updatedAt,
          generated_at      AS generatedAt
        FROM rank_top3_by_category
        WHERE store_category_id = :categoryId
        ORDER BY rank
        """, nativeQuery = true)
    List<RankTop3View> findByCategory(@Param("categoryId") Integer categoryId);

    @Query(value = """
        SELECT
          store_category_id AS storeCategoryId,
          rank              AS rank,
          store_id          AS storeId,
          store_name        AS storeName,
          avg_rating        AS avgRating,
          rating_count      AS ratingCount,
          updated_at        AS updatedAt,
          generated_at      AS generatedAt
        FROM rank_top3_by_category
        ORDER BY store_category_id, rank
        """, nativeQuery = true)
    List<RankTop3View> findAllRanks();
}
