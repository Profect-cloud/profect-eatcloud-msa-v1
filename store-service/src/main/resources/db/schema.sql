CREATE EXTENSION IF NOT EXISTS postgis;
---- Table Type 1 : Enum To Table


CREATE TABLE p_time
(
    p_time_id  UUID PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_by VARCHAR(100) NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

CREATE TABLE order_status_codes
(
    code         VARCHAR(30) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    sort_order   INTEGER     NOT NULL,
    is_active    BOOLEAN     NOT NULL DEFAULT true,
    p_time_id    UUID        NOT NULL,
    CONSTRAINT fk_order_status_codes_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE order_type_codes
(
    code         VARCHAR(30) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    sort_order   INTEGER     NOT NULL,
    is_active    BOOLEAN     NOT NULL DEFAULT true,
    p_time_id    UUID        NOT NULL,
    CONSTRAINT fk_order_type_codes_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE payment_status_codes
(
    code         VARCHAR(30) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    sort_order   INTEGER     NOT NULL,
    is_active    BOOLEAN     NOT NULL DEFAULT true,
    p_time_id    UUID        NOT NULL,
    CONSTRAINT fk_payment_status_codes_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE payment_method_codes
(
    code         VARCHAR(30) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    sort_order   INTEGER     NOT NULL,
    is_active    BOOLEAN     NOT NULL DEFAULT true,
    p_time_id    UUID        NOT NULL,
    CONSTRAINT fk_payment_method_codes_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_categories
(
    category_id   UUID PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    sort_order    INTEGER      NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    p_time_id     UUID         NOT NULL,
    CONSTRAINT fk_p_categories_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);
-----------------------------------------------

-- 9. Menu categories
CREATE TABLE p_menu_category
(
    code         VARCHAR(30) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    sort_order   INTEGER     NOT NULL,
    is_active    BOOLEAN     NOT NULL DEFAULT true,
    p_time_id    UUID        NOT NULL,
    CONSTRAINT fk_menu_cat_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE delivery_areas
(
    area_id   UUID PRIMARY KEY,
    area_name VARCHAR(100) NOT NULL,
    p_time_id UUID         NOT NULL,
    CONSTRAINT fk_delivery_areas_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

--------- Table Type 2 : Real Data

CREATE TABLE p_admins
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(20) UNIQUE NOT NULL,
    email        VARCHAR(255)       NOT NULL,
    password     VARCHAR(255)       NOT NULL,
    phone_number VARCHAR(18),
    position     VARCHAR(50),
    p_time_id    UUID               NOT NULL,
    CONSTRAINT fk_admins_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_customer
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(20) UNIQUE NOT NULL,
    nickname     VARCHAR(100),
    email        VARCHAR(255),
    password     VARCHAR(255)       NOT NULL,
    phone_number VARCHAR(18),
    points       INT,
    p_time_id    UUID               NOT NULL,
    CONSTRAINT fk_p_users_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_addresses
(
    id          UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    zipcode     VARCHAR(10),
    road_addr   VARCHAR(500),
    detail_addr VARCHAR(200),
    is_selected BOOLEAN DEFAULT FALSE,
    p_time_id   UUID NOT NULL,
    CONSTRAINT fk_p_addresses_user FOREIGN KEY (customer_id) REFERENCES p_customer (id),
    CONSTRAINT fk_p_addresses_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_cart
(
    cart_id     UUID PRIMARY KEY,
    customer_id UUID  NOT NULL,
    cart_items  JSONB NOT NULL,
    p_time_id   UUID  NOT NULL,
    CONSTRAINT fk_cart_user FOREIGN KEY (customer_id) REFERENCES p_customer (id),
    CONSTRAINT fk_cart_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_stores
(
    store_id      UUID PRIMARY KEY,
    store_name    VARCHAR(200) NOT NULL,
    store_address VARCHAR(300),
    phone_number  VARCHAR(18),
    category_id   UUID,
    min_cost      INTEGER      NOT NULL DEFAULT 0,
    description   TEXT,
    store_lat     DOUBLE PRECISION,
    store_lon     DOUBLE PRECISION,
    open_status   BOOLEAN,
    open_time     TIME         NOT NULL,
    close_time    TIME         NOT NULL,
    p_time_id     UUID         NOT NULL,
    location      GEOGRAPHY(Point, 4326),
    CONSTRAINT fk_p_stores_categories FOREIGN KEY (category_id) REFERENCES p_categories (category_id),
    CONSTRAINT fk_p_stores_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_managers
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(20) UNIQUE NOT NULL,
    email        VARCHAR(255)       NOT NULL,
    password     VARCHAR(255)       NOT NULL,
    phone_number VARCHAR(18),
    store_id     UUID, -- 담당 매장 (선택)
    p_time_id    UUID               NOT NULL,
    CONSTRAINT fk_managers_store FOREIGN KEY (store_id) REFERENCES p_stores (store_id),
    CONSTRAINT fk_managers_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_menus
(
    menu_id            UUID PRIMARY KEY,
    store_id           UUID         NOT NULL,
    menu_num           INTEGER      NOT NULL,
    menu_name          VARCHAR(200) NOT NULL,
    menu_category_code VARCHAR(30)  NOT NULL,
    price              INTEGER      NOT NULL,
    description        TEXT,
    is_available       BOOLEAN      NOT NULL DEFAULT true,
    image_url          VARCHAR(500),
    p_time_id          UUID         NOT NULL,
    CONSTRAINT fk_menus_store FOREIGN KEY (store_id) REFERENCES p_stores (store_id),
    CONSTRAINT fk_menus_menu_cat FOREIGN KEY (menu_category_code) REFERENCES p_menu_category (code),
    CONSTRAINT fk_menus_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);



CREATE TABLE p_payment_requests
(
    payment_request_id UUID PRIMARY KEY,
    order_id           UUID         NOT NULL,
    pg_provider        VARCHAR(100) NOT NULL,
    request_payload    JSONB        NOT NULL,
    redirect_url       TEXT,
    status             VARCHAR(50)  NOT NULL,
    requested_at       TIMESTAMP    NOT NULL,
    responded_at       TIMESTAMP,
    failure_reason     TEXT,
    p_time_id          UUID         NOT NULL,
    CONSTRAINT fk_pr_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_payments
(
    payment_id           UUID PRIMARY KEY,
    customer_id          UUID        NOT NULL,
    payment_request_id   UUID        NOT NULL,
    payment_status       VARCHAR(30) NOT NULL,
    payment_method       VARCHAR(30) NOT NULL,
    total_amount         INTEGER     NOT NULL,
    pg_transaction_id    VARCHAR(100),
    approval_code        VARCHAR(50),
    card_info            JSONB,
    redirect_url         TEXT,
    receipt_url          TEXT,
    requested_at         TIMESTAMP,
    approved_at          TIMESTAMP,
    failed_at            TIMESTAMP,
    failure_reason       TEXT,
    offline_payment_note TEXT,
    p_time_id            UUID        NOT NULL,
    CONSTRAINT fk_payments_user FOREIGN KEY (customer_id) REFERENCES p_customer (id),
    CONSTRAINT fk_payments_pr FOREIGN KEY (payment_request_id) REFERENCES p_payment_requests (payment_request_id),
    CONSTRAINT fk_payments_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);


CREATE TABLE p_orders
(
    order_id             UUID PRIMARY KEY,
    order_number         VARCHAR(50) UNIQUE NOT NULL,
    customer_id          UUID               NOT NULL,
    store_id             UUID               NOT NULL,
    payment_id           UUID,
    order_status         VARCHAR(30)        NOT NULL,
    order_type           VARCHAR(30)        NOT NULL,
    order_menu_list      JSONB              NOT NULL,
    total_price          INTEGER            NOT NULL DEFAULT 0,
    use_points           BOOLEAN            NOT NULL DEFAULT false,
    points_to_use        INTEGER            NOT NULL DEFAULT 0,
    final_payment_amount INTEGER            NOT NULL DEFAULT 0,
    p_time_id            UUID               NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (customer_id) REFERENCES p_customer (id),
    CONSTRAINT fk_orders_store FOREIGN KEY (store_id) REFERENCES p_stores (store_id),
    CONSTRAINT fk_orders_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

-- 4-1) p_orders.payment_id → p_payments.payment_id
ALTER TABLE p_orders
    ADD CONSTRAINT fk_orders_payment
        FOREIGN KEY (payment_id)
            REFERENCES p_payments (payment_id);

-- 4-2) p_payment_requests.order_id → p_orders(order_id)
ALTER TABLE p_payment_requests
    ADD CONSTRAINT fk_pr_orders
        FOREIGN KEY (order_id)
            REFERENCES p_orders (order_id);

CREATE TABLE p_delivery_orders
(
    order_id                   UUID PRIMARY KEY,
    delivery_fee               DECIMAL(10, 2) DEFAULT 0,
    delivery_requests          TEXT,
    zipcode                    VARCHAR(10),
    road_addr                  VARCHAR(500),
    detail_addr                VARCHAR(200),
    estimated_delivery_time    TIMESTAMP,
    estimated_preparation_time INTEGER,
    canceled_at                TIMESTAMP,
    canceled_by                VARCHAR(100),
    cancel_reason              TEXT,
    p_time_id                  UUID NOT NULL,
    CONSTRAINT fk_do_orders FOREIGN KEY (order_id) REFERENCES p_orders (order_id),
    CONSTRAINT fk_do_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);


CREATE TABLE p_store_delivery_areas
(
    store_id     UUID    NOT NULL,
    area_id      UUID    NOT NULL,
    delivery_fee INTEGER NOT NULL DEFAULT 0,
    p_time_id    UUID    NOT NULL,
    CONSTRAINT pk_store_delivery PRIMARY KEY (store_id, area_id),
    CONSTRAINT fk_sda_store FOREIGN KEY (store_id) REFERENCES p_stores (store_id),
    CONSTRAINT fk_sda_area FOREIGN KEY (area_id) REFERENCES delivery_areas (area_id),
    CONSTRAINT fk_sda_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_pickup_orders
(
    order_id              UUID PRIMARY KEY,
    pickup_requests       TEXT,
    estimated_pickup_time TIMESTAMP,
    canceled_at           TIMESTAMP,
    canceled_by           VARCHAR(100),
    cancel_reason         TEXT,
    p_time_id             UUID NOT NULL,
    CONSTRAINT fk_po_orders FOREIGN KEY (order_id) REFERENCES p_orders (order_id),
    CONSTRAINT fk_po_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_reviews
(
    review_id UUID PRIMARY KEY,
    order_id  UUID          NOT NULL,
    rating    NUMERIC(2, 1) NOT NULL,
    content   TEXT          NOT NULL,
    p_time_id UUID          NOT NULL,
    CONSTRAINT fk_reviews_orders FOREIGN KEY (order_id) REFERENCES p_orders (order_id),
    CONSTRAINT fk_reviews_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_ai_responses
(
    ai_response_id UUID PRIMARY KEY,
    description    TEXT NOT NULL,
    p_time_id      UUID NOT NULL,
    CONSTRAINT fk_ai_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE daily_store_sales
(
    sale_date    DATE           NOT NULL,
    store_id     UUID           NOT NULL,
    order_count  INTEGER        NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    p_time_id    UUID           NOT NULL,
    CONSTRAINT pk_daily_store PRIMARY KEY (sale_date, store_id),
    CONSTRAINT fk_dss_store FOREIGN KEY (store_id) REFERENCES p_stores (store_id),
    CONSTRAINT fk_dss_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE daily_menu_sales
(
    sale_date     DATE           NOT NULL,
    store_id      UUID           NOT NULL,
    menu_id       UUID           NOT NULL,
    quantity_sold INTEGER        NOT NULL,
    total_amount  DECIMAL(12, 2) NOT NULL,
    p_time_id     UUID           NOT NULL,
    CONSTRAINT pk_daily_menu PRIMARY KEY (sale_date, store_id, menu_id),
    CONSTRAINT fk_dms_store FOREIGN KEY (store_id) REFERENCES p_stores (store_id),
    CONSTRAINT fk_dms_menu FOREIGN KEY (menu_id) REFERENCES p_menus (menu_id),
    CONSTRAINT fk_dms_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);


-- added at 8/1
CREATE TABLE p_manager_store_applications
(
    application_id       UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    -- ■ Manager 신청 정보
    manager_name         VARCHAR(20)  NOT NULL,
    manager_email        VARCHAR(255) NOT NULL,
    manager_password     VARCHAR(255) NOT NULL,
    manager_phone_number VARCHAR(18),

    -- ■ Store 신청 정보
    store_name           VARCHAR(200) NOT NULL,
    store_address        VARCHAR(300),
    store_phone_number   VARCHAR(18),
    category_id          UUID,
    description          TEXT,

    -- ■ 심사 상태
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    reviewer_admin_id    UUID,                                    -- 처리한 Admin ID
    review_comment       TEXT,                                    -- Admin 코멘트

    -- ■ BaseTimeEntity 공통 시간 관리
    p_time_id            UUID         NOT NULL,
    CONSTRAINT fk_applications_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

