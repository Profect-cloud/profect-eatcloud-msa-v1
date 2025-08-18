-- STORES service
-- PostGIS 타입 사용 시: CREATE EXTENSION IF NOT EXISTS postgis; (DB 레벨)

CREATE TABLE p_stores (
  store_id      UUID PRIMARY KEY,
  store_name    VARCHAR(200) NOT NULL,
  store_address VARCHAR(300),
  phone_number  VARCHAR(18),
  category_id   UUID NOT NULL,   -- 논리참조: admin.p_categories.category_id
  min_cost      INTEGER      NOT NULL DEFAULT 0,
  description   TEXT,
  store_lat     DOUBLE PRECISION,
  store_lon     DOUBLE PRECISION,
  open_status   BOOLEAN,
  open_time     TIME NOT NULL,
  close_time    TIME NOT NULL,
  p_time_id     UUID NOT NULL,   -- FK 제거됨
  location      GEOGRAPHY(Point, 4326)
);

CREATE TABLE p_menus (
  menu_id          UUID PRIMARY KEY,
  store_id         UUID         NOT NULL,
  menu_num         INTEGER      NOT NULL,
  menu_name        VARCHAR(200) NOT NULL,
  menu_category_id INT          NOT NULL,  -- 논리참조: admin.p_menu_category.menu_category_id
  price            INTEGER      NOT NULL,
  description      TEXT,
  is_available     BOOLEAN      NOT NULL DEFAULT true,
  image_url        VARCHAR(500),
  p_time_id        UUID         NOT NULL   -- FK 제거됨
);
ALTER TABLE p_menus
  ADD CONSTRAINT fk_menu_store FOREIGN KEY (store_id) REFERENCES p_stores (store_id);
CREATE INDEX idx_menus_store_category ON p_menus(store_id, menu_category_id);

CREATE TABLE delivery_areas (
  area_id   UUID PRIMARY KEY,
  area_name VARCHAR(100) NOT NULL,
  p_time_id UUID         NOT NULL   -- FK 제거됨
);

CREATE TABLE p_store_delivery_areas (
  store_id     UUID NOT NULL,
  area_id      UUID NOT NULL,
  delivery_fee INTEGER NOT NULL DEFAULT 0,
  p_time_id    UUID NOT NULL     -- FK 제거됨
);
ALTER TABLE p_store_delivery_areas
  ADD CONSTRAINT pk_store_delivery PRIMARY KEY (store_id, area_id);
ALTER TABLE p_store_delivery_areas
  ADD CONSTRAINT fk_sda_store FOREIGN KEY (store_id) REFERENCES p_stores (store_id);
ALTER TABLE p_store_delivery_areas
  ADD CONSTRAINT fk_sda_area  FOREIGN KEY (area_id)  REFERENCES delivery_areas (area_id);

-- 집계(프로젝션)
CREATE TABLE daily_store_sales (
  sale_date    DATE           NOT NULL,
  store_id     UUID           NOT NULL, -- 논리참조
  order_count  INTEGER        NOT NULL,
  total_amount DECIMAL(12,2)  NOT NULL,
  p_time_id    UUID           NOT NULL  -- FK 제거됨
);
ALTER TABLE daily_store_sales
  ADD CONSTRAINT pk_daily_store PRIMARY KEY (sale_date, store_id);
CREATE INDEX idx_dss_store ON daily_store_sales(store_id);

CREATE TABLE daily_menu_sales (
  sale_date     DATE           NOT NULL,
  store_id      UUID           NOT NULL, -- 논리참조
  menu_id       UUID           NOT NULL, -- 논리참조
  quantity_sold INTEGER        NOT NULL,
  total_amount  DECIMAL(12,2)  NOT NULL,
  p_time_id     UUID           NOT NULL  -- FK 제거됨
);
ALTER TABLE daily_menu_sales
  ADD CONSTRAINT pk_daily_menu PRIMARY KEY (sale_date, store_id, menu_id);
CREATE INDEX idx_dms_store_date ON daily_menu_sales(store_id, sale_date);

-- 간단 AI 설명(필요 시 교체 가능)
CREATE TABLE p_ai_responses (
  ai_response_id UUID PRIMARY KEY,
  description    TEXT NOT NULL,
  p_time_id      UUID NOT NULL   -- FK 제거됨
);
