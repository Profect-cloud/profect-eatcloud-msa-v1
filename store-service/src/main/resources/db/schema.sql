-- stores/schema.sql
CREATE SCHEMA IF NOT EXISTS stores;
SET search_path TO stores;

-- PostGIS for geography/point
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS p_stores (
  store_id       UUID PRIMARY KEY,
  store_name     VARCHAR(200) NOT NULL,
  store_address  VARCHAR(300),
  phone_number   VARCHAR(18),
  category_id    INT NOT NULL, -- logical ref -> admin.p_store_categories.id (top-level)
  min_cost       INTEGER NOT NULL DEFAULT 0,
  description    TEXT,
  store_lat      DOUBLE PRECISION,
  store_lon      DOUBLE PRECISION,
  open_status    BOOLEAN,
  open_time      TIME NOT NULL,
  close_time     TIME NOT NULL,
  location       geography(Point, 4326),
  created_at     TIMESTAMP    NOT NULL DEFAULT now(),
  created_by     VARCHAR(100) NOT NULL,
  updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
  updated_by     VARCHAR(100) NOT NULL,
  deleted_at     TIMESTAMP,
  deleted_by     VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS p_menus (
  menu_id          UUID PRIMARY KEY,
  store_id         UUID NOT NULL REFERENCES p_stores(store_id),
  menu_num         INTEGER NOT NULL,
  menu_name        VARCHAR(200) NOT NULL,
  menu_category_code  VARCHAR(100) NOT NULL,
  price            INTEGER NOT NULL,
  description      TEXT,
  is_available     BOOLEAN NOT NULL DEFAULT TRUE,
  image_url        VARCHAR(500),
  created_at       TIMESTAMP    NOT NULL DEFAULT now(),
  created_by       VARCHAR(100) NOT NULL,
  updated_at       TIMESTAMP    NOT NULL DEFAULT now(),
  updated_by       VARCHAR(100) NOT NULL,
  deleted_at       TIMESTAMP,
  deleted_by       VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS idx_menus_store_category ON p_menus (store_id, menu_category_code);

CREATE TABLE IF NOT EXISTS delivery_areas (
  area_id     UUID PRIMARY KEY,
  area_name   VARCHAR(100) NOT NULL,
  created_at  TIMESTAMP    NOT NULL DEFAULT now(),
  created_by  VARCHAR(100) NOT NULL,
  updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
  updated_by  VARCHAR(100) NOT NULL,
  deleted_at  TIMESTAMP,
  deleted_by  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS p_store_delivery_areas (
  store_id     UUID NOT NULL REFERENCES p_stores(store_id),
  area_id      UUID NOT NULL REFERENCES delivery_areas(area_id),
  delivery_fee INTEGER NOT NULL DEFAULT 0,
  created_at   TIMESTAMP    NOT NULL DEFAULT now(),
  created_by   VARCHAR(100) NOT NULL,
  updated_at   TIMESTAMP    NOT NULL DEFAULT now(),
  updated_by   VARCHAR(100) NOT NULL,
  deleted_at   TIMESTAMP,
  deleted_by   VARCHAR(100),
  PRIMARY KEY (store_id, area_id)
);

-- 집계 테이블(논리 참조만 유지)
CREATE TABLE IF NOT EXISTS daily_store_sales (
  sale_date    DATE NOT NULL,
  store_id     UUID NOT NULL, -- logical ref -> stores.p_stores.store_id
  order_count  INTEGER NOT NULL,
  total_amount NUMERIC(12,2) NOT NULL,
  created_at   TIMESTAMP    NOT NULL DEFAULT now(),
  created_by   VARCHAR(100) NOT NULL,
  updated_at   TIMESTAMP    NOT NULL DEFAULT now(),
  updated_by   VARCHAR(100) NOT NULL,
  deleted_at   TIMESTAMP,
  deleted_by   VARCHAR(100),
  PRIMARY KEY (sale_date, store_id)
);
CREATE INDEX IF NOT EXISTS idx_daily_store_sales_store ON daily_store_sales (store_id);

CREATE TABLE IF NOT EXISTS daily_menu_sales (
  sale_date     DATE NOT NULL,
  store_id      UUID NOT NULL, -- logical ref -> stores.p_stores.store_id
  menu_id       UUID NOT NULL, -- logical ref -> stores.p_menus.menu_id
  quantity_sold INTEGER NOT NULL,
  total_amount  NUMERIC(12,2) NOT NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT now(),
  created_by    VARCHAR(100) NOT NULL,
  updated_at    TIMESTAMP    NOT NULL DEFAULT now(),
  updated_by    VARCHAR(100) NOT NULL,
  deleted_at    TIMESTAMP,
  deleted_by    VARCHAR(100),
  PRIMARY KEY (sale_date, store_id, menu_id)
);
CREATE INDEX IF NOT EXISTS idx_daily_menu_sales_store_date
  ON daily_menu_sales (store_id, sale_date);

CREATE TABLE IF NOT EXISTS p_ai_responses (
  ai_response_id UUID PRIMARY KEY,
  description    TEXT NOT NULL,
  created_at     TIMESTAMP    NOT NULL DEFAULT now(),
  created_by     VARCHAR(100) NOT NULL,
  updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
  updated_by     VARCHAR(100) NOT NULL,
  deleted_at     TIMESTAMP,
  deleted_by     VARCHAR(100)
);
