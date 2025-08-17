\c manager_db;

-- MANAGERS service

CREATE TABLE p_managers (
  id           UUID PRIMARY KEY,
  name         VARCHAR(20) UNIQUE NOT NULL,
  email        VARCHAR(255)       NOT NULL,
  password     VARCHAR(255)       NOT NULL,
  phone_number VARCHAR(18),
  store_id     UUID,  -- 논리참조: stores.p_stores.store_id
  p_time_id    UUID   NOT NULL    -- FK 제거됨
);

