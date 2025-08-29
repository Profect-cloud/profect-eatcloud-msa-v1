CREATE TABLE IF NOT EXISTS rank_top3_by_category (
                                                     store_category_id INT NOT NULL,
                                                     rank              INT NOT NULL CHECK (rank BETWEEN 1 AND 3),
                                                     store_id          UUID NOT NULL,
                                                     store_name        TEXT NOT NULL,
                                                     avg_rating        NUMERIC(3,2) NOT NULL,
                                                     rating_count      INT NOT NULL,
                                                     updated_at        TIMESTAMP NOT NULL,
                                                     generated_at      TIMESTAMP NOT NULL DEFAULT now(),
                                                     PRIMARY KEY (store_category_id, rank)
);

WITH ranked AS (
    SELECT
        s.store_category_id, s.store_id, s.store_name,
        s.avg_rating, s.rating_count, s.updated_at,
        ROW_NUMBER() OVER (
            PARTITION BY s.store_category_id
            ORDER BY s.avg_rating DESC, s.rating_count DESC, s.store_name ASC
            ) AS rn
    FROM p_stores s
    WHERE COALESCE(s.open_status, TRUE) = TRUE
      AND s.rating_count >= 3
)
INSERT INTO rank_top3_by_category AS r
(store_category_id, rank, store_id, store_name, avg_rating, rating_count, updated_at, generated_at)
SELECT
    store_category_id, rn, store_id, store_name, avg_rating, rating_count, updated_at, now()
FROM ranked
WHERE rn <= 3
ON CONFLICT (store_category_id, rank) DO UPDATE
    SET store_id     = EXCLUDED.store_id,
        store_name   = EXCLUDED.store_name,
        avg_rating   = EXCLUDED.avg_rating,
        rating_count = EXCLUDED.rating_count,
        updated_at   = EXCLUDED.updated_at,
        generated_at = EXCLUDED.generated_at;
