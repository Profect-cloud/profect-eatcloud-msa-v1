\c store_db;

-- STORES service schema.sql
-- Physical split: store/menu tables. No cross-schema FKs.
-- NOTE: Uses PostGIS 'GEOGRAPHY'; ensure CREATE EXTENSION postgis; at DB level (admin user).

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

CREATE TABLE p_categories
(
    category_id   UUID PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    sort_order    INTEGER      NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    p_time_id     UUID         NOT NULL,
    CONSTRAINT fk_p_categories_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

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

CREATE TABLE p_ai_responses
(
    ai_response_id UUID PRIMARY KEY,
    description    TEXT NOT NULL,
    p_time_id      UUID NOT NULL,
    CONSTRAINT fk_ai_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

