CREATE TABLE IF NOT EXISTS fact_store_reviews (
                                                  review_id          UUID PRIMARY KEY,
                                                  order_id           UUID NOT NULL,
                                                  store_id           UUID NOT NULL,
                                                  customer_id        UUID,                 -- NULL 허용
                                                  rating             NUMERIC(2,1) NOT NULL CHECK (rating >= 0 AND rating <= 5),
                                                  content            TEXT,
                                                  review_created_at  TIMESTAMP NOT NULL,
                                                  review_updated_at  TIMESTAMP,
                                                  order_created_at   TIMESTAMP,
                                                  order_updated_at   TIMESTAMP,
                                                  updated_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_fact_reviews_store ON fact_store_reviews (store_id);

INSERT INTO fact_store_reviews AS f (
    review_id, order_id, store_id, customer_id, rating,
    content, review_created_at, review_updated_at, order_created_at, order_updated_at, updated_at
)
SELECT
    s.review_id, s.order_id, s.store_id, s.customer_id,
    s.rating, NULLIF(TRIM(s.content), ''),
    s.review_created_at, s.review_updated_at, s.order_created_at, s.order_updated_at,
    COALESCE(s.review_updated_at, s.updated_at, now())
FROM stg_order_reviews_raw s
WHERE s.review_id IS NOT NULL
  AND s.rating IS NOT NULL
  AND s.review_created_at IS NOT NULL
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
                                      updated_at         = EXCLUDED.updated_at;
