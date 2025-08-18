\c customer_db;

-- USERS service

CREATE TABLE p_customer (
  id           UUID PRIMARY KEY,
  name         VARCHAR(20) UNIQUE NOT NULL,
  nickname     VARCHAR(100),
  email        VARCHAR(255),
  password     VARCHAR(255)       NOT NULL,
  phone_number VARCHAR(18),
  points       INTEGER,
  p_time_id    UUID               NOT NULL  -- FK 제거됨
);

CREATE TABLE p_addresses (
  id          UUID PRIMARY KEY,
  customer_id UUID NOT NULL,
  zipcode     VARCHAR(10),
  road_addr   VARCHAR(500),
  detail_addr VARCHAR(200),
  is_selected BOOLEAN DEFAULT FALSE,
  p_time_id   UUID NOT NULL       -- FK 제거됨
);
ALTER TABLE p_addresses
  ADD CONSTRAINT fk_addr_customer
  FOREIGN KEY (customer_id) REFERENCES p_customer (id);

