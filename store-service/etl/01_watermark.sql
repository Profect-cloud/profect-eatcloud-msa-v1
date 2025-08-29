CREATE TABLE IF NOT EXISTS etl_watermarks (
                                              source_table TEXT PRIMARY KEY,
                                              last_updated TIMESTAMP NOT NULL
);

INSERT INTO etl_watermarks (source_table, last_updated)
VALUES ('p_reviews', '2000-01-01'::timestamp)
ON CONFLICT (source_table) DO NOTHING;