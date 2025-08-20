CREATE SCHEMA IF NOT EXISTS customer;
SET search_path TO customer;

CREATE TABLE IF NOT EXISTS p_customer (
    id           UUID PRIMARY KEY,
    name         VARCHAR(20) UNIQUE NOT NULL,
    nickname     VARCHAR(100),
    email        VARCHAR(255),
    password     VARCHAR(255) NOT NULL,
    phone_number VARCHAR(18),
    points       INTEGER,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    created_by   VARCHAR(100) NOT NULL,
    updated_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_by   VARCHAR(100) NOT NULL,
    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS p_addresses (
    id           UUID PRIMARY KEY,
    customer_id  UUID NOT NULL REFERENCES p_customer(id),
    zipcode      VARCHAR(10),
    road_addr    VARCHAR(500),
    detail_addr  VARCHAR(200),
    is_selected  BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    created_by   VARCHAR(100) NOT NULL,
    updated_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_by   VARCHAR(100) NOT NULL,
    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100)
    );
CREATE INDEX IF NOT EXISTS idx_addresses_customer ON p_addresses(customer_id);
