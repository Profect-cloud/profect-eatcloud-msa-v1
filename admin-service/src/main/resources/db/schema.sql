-- ADMIN service
-- 참고: gen_random_uuid() 사용 시 DB 레벨에서 pgcrypto 확장 필요
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE p_admins (
  id           UUID PRIMARY KEY,
  name         VARCHAR(20) UNIQUE NOT NULL,
  email        VARCHAR(255)       NOT NULL,
  password     VARCHAR(255)       NOT NULL,
  phone_number VARCHAR(18),
  position     VARCHAR(50),
  p_time_id    UUID               NOT NULL  -- FK 제거됨
);

-- 매니저 신청(헤더/심사)
CREATE TABLE p_manager_applications (
  application_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  manager_name         VARCHAR(20)  NOT NULL,
  manager_email        VARCHAR(255) NOT NULL,
  manager_password     VARCHAR(255) NOT NULL,
  manager_phone_number VARCHAR(18),

  status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING|APPROVED|REJECTED
  reviewer_admin_id    UUID,                                     -- 내부 FK 유지
  review_comment       TEXT,

  p_time_id            UUID         NOT NULL                     -- FK 제거됨
);
CREATE UNIQUE INDEX ux_mgrapp_manager_email ON p_manager_applications(manager_email);
ALTER TABLE p_manager_applications
  ADD CONSTRAINT fk_mgrapp_reviewer
  FOREIGN KEY (reviewer_admin_id) REFERENCES p_admins (id);

-- 스토어 신청 상세 (1:1: application_id 동일)
CREATE TABLE p_store_applications (
  application_id      UUID PRIMARY KEY,                          -- 내부 FK 유지(헤더와 동일 키)
  store_name          VARCHAR(200) NOT NULL,
  store_address       VARCHAR(300),
  store_phone_number  VARCHAR(18),
  category_id         UUID,            -- 논리참조: admin.p_categories.category_id
  description         TEXT,

  status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  reviewer_admin_id   UUID,            -- 내부 FK 유지
  review_comment      TEXT,

  p_time_id           UUID NOT NULL     -- FK 제거됨
);
ALTER TABLE p_store_applications
  ADD CONSTRAINT fk_storeapp_header
  FOREIGN KEY (application_id) REFERENCES p_manager_applications (application_id);
ALTER TABLE p_store_applications
  ADD CONSTRAINT fk_storeapp_reviewer
  FOREIGN KEY (reviewer_admin_id) REFERENCES p_admins (id);

-- 매장 대분류
CREATE TABLE p_categories (
  category_id        UUID PRIMARY KEY,
  category_name      VARCHAR(100) NOT NULL,
  sort_order         INTEGER      NOT NULL,
  is_active          BOOLEAN      NOT NULL DEFAULT true,
  total_store_count  INTEGER      NOT NULL DEFAULT 0,
  p_time_id          UUID         NOT NULL   -- FK 제거됨
);

-- 메뉴 소분류
CREATE TABLE p_menu_category (
  menu_category_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  menu_category_name  VARCHAR(50)  NOT NULL,
  sort_order          INTEGER      NOT NULL,
  is_active           BOOLEAN      NOT NULL DEFAULT true,
  total_store_count   INTEGER      NOT NULL DEFAULT 0,
  p_time_id           UUID         NOT NULL   -- FK 제거됨
);
CREATE UNIQUE INDEX ux_pmc_name ON p_menu_category(menu_category_name);
