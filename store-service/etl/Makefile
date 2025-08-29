DB_URL_STORE=postgresql://postgres:postgres@localhost:5432/store_db

fdw:
	psql $(DB_URL_STORE) -v ON_ERROR_STOP=1 -f etl/00_fdw_order.sql

watermark:
	psql $(DB_URL_STORE) -v ON_ERROR_STOP=1 -f etl/01_watermark.sql

ingest:
	psql $(DB_URL_STORE) -v ON_ERROR_STOP=1 -f etl/10_staging_load_order_reviews.sql
	psql $(DB_URL_STORE) -v ON_ERROR_STOP=1 -f etl/20_upsert_fact_store_reviews.sql
	psql $(DB_URL_STORE) -v ON_ERROR_STOP=1 -f etl/30_rebuild_agg_store_ratings.sql
	psql $(DB_URL_STORE) -v ON_ERROR_STOP=1 -f etl/40_sync_ratings_to_p_stores.sql

rankings:
	psql $(DB_URL_STORE) -v ON_ERROR_STOP=1 -f etl/50_generate_rank_top3_by_category.sql

quality:
	psql $(DB_URL_STORE) -v ON_ERROR_STOP=1 -f etl/60_quality_freshness_checks.sql

all: fdw watermark ingest rankings
