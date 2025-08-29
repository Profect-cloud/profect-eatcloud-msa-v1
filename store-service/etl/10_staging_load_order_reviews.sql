-- 스테이징 (멱등)
CREATE TABLE IF NOT EXISTS stg_order_reviews_raw (
                                                     review_id          UUID PRIMARY KEY,
                                                     order_id           UUID NOT NULL,
                                                     store_id           UUID NOT NULL,
                                                     customer_id        UUID,
                                                     rating             NUMERIC(2,1),
                                                     content            TEXT,
                                                     review_created_at  TIMESTAMP,
                                                     review_updated_at  TIMESTAMP,
                                                     order_created_at   TIMESTAMP,
                                                     order_updated_at   TIMESTAMP,
                                                     updated_at         TIMESTAMP NOT NULL,   -- 증분 기준 열
                                                     ingest_ts          TIMESTAMP NOT NULL DEFAULT now(),
                                                     source             TEXT NOT NULL DEFAULT 'ext_order.p_reviews + ext_order.p_orders'
);

CREATE INDEX IF NOT EXISTS idx_stg_reviews_store  ON stg_order_reviews_raw (store_id);
CREATE INDEX IF NOT EXISTS idx_stg_reviews_upd    ON stg_order_reviews_raw (updated_at);

WITH wm AS (
    SELECT COALESCE((SELECT last_updated FROM etl_watermarks WHERE source_table='p_reviews'),
                    '2000-01-01'::timestamp) AS last_updated
)
INSERT INTO stg_order_reviews_raw AS s (
    review_id, order_id, store_id, customer_id, rating, content,
    review_created_at, review_updated_at, order_created_at, order_updated_at,
    updated_at
)
SELECT
    rv.review_id,
    rv.order_id,
    o.store_id,
    o.customer_id,
    rv.rating,
    rv.content,
    rv.created_at,
    rv.updated_at,
    o.created_at,
    o.updated_at,
    COALESCE(rv.updated_at, rv.created_at, o.updated_at, o.created_at) AS updated_at
FROM ext_order.p_reviews rv
         JOIN ext_order.p_orders  o ON o.order_id = rv.order_id
         CROSS JOIN wm
WHERE COALESCE(rv.updated_at, rv.created_at, o.updated_at, o.created_at) >= wm.last_updated
ON CONFLICT (review_id) DO UPDATE SET
                                      order_id           = EXCLUDED.order_id,
                                      store_id           = EXCLUDED.store_id,
                                      customer_id        = EXCLUDED.customer_id,
                                      rating             = EXCLUDED.rating,
                                      content            = EXCLUDED.content,
                                      review_created_at  = EXCLUDED.review_created_at,
                                      review_updated_at  = EXCLUDED.review_updated_at,
                                      order_created_at   = EXCLUDED.order_created_at,
                                      order_updated_at   = EXCLUDED.order_updated_at,
                                      updated_at         = EXCLUDED.updated_at,
                                      ingest_ts          = now();

-- 워터마크 갱신: 실제 반영된 최대 updated_at
UPDATE etl_watermarks
SET last_updated = GREATEST(
        COALESCE((SELECT MAX(updated_at) FROM stg_order_reviews_raw), '2000-01-01'::timestamp),
        (SELECT last_updated FROM etl_watermarks WHERE source_table='p_reviews')
                   )
WHERE source_table='p_reviews';
