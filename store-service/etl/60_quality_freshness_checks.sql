-- 품질1: avg_rating NULL 없음
SELECT COUNT(*) AS null_avg_rating FROM p_stores WHERE avg_rating IS NULL;

-- 품질2: fact 중복키 없음
SELECT COUNT(*) AS dup_reviews
FROM (
         SELECT review_id FROM fact_store_reviews GROUP BY review_id HAVING COUNT(*)>1
     ) d;

-- 신선도 <24h
SELECT MAX(updated_at) AS last_agg_time,
       (now() - MAX(updated_at)) < interval '24h' AS fresh_under_24h
FROM agg_store_ratings;
