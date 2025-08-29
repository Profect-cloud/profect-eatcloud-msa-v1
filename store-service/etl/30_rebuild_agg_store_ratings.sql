CREATE TABLE IF NOT EXISTS agg_store_ratings (
                                                 store_id     UUID PRIMARY KEY,
                                                 rating_sum   NUMERIC(12,2) NOT NULL,
                                                 rating_count INT           NOT NULL,
                                                 avg_rating   NUMERIC(3,2)  NOT NULL,
                                                 updated_at   TIMESTAMP     NOT NULL DEFAULT now()
);

INSERT INTO agg_store_ratings AS a
(store_id, rating_sum, rating_count, avg_rating, updated_at)
SELECT
    store_id,
    SUM(rating)::NUMERIC(12,2),
    COUNT(*),
    ROUND(AVG(rating)::NUMERIC, 2),
    now()
FROM fact_store_reviews
WHERE rating BETWEEN 0 AND 5
GROUP BY store_id
ON CONFLICT (store_id) DO UPDATE
    SET rating_sum   = EXCLUDED.rating_sum,
        rating_count = EXCLUDED.rating_count,
        avg_rating   = EXCLUDED.avg_rating,
        updated_at   = EXCLUDED.updated_at;
