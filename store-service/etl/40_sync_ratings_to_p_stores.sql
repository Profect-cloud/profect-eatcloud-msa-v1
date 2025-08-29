UPDATE p_stores s
SET rating_sum   = a.rating_sum,
    rating_count = a.rating_count,
    avg_rating   = a.avg_rating,
    updated_at   = now(),
    updated_by   = 'etl'
FROM agg_store_ratings a
WHERE s.store_id = a.store_id
  AND (s.rating_sum   IS DISTINCT FROM a.rating_sum
    OR  s.rating_count IS DISTINCT FROM a.rating_count
    OR  s.avg_rating   IS DISTINCT FROM a.avg_rating);
