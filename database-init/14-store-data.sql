\c store_db;

-- ================================
-- store-db/init/data.sql
-- ================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS postgis;

-- 고정 네임스페이스 (order-db와 동일 값 사용할 것)
-- 이름 기반 v5 UUID를 위해 사용
WITH ns AS (
    SELECT '00000000-0000-0000-0000-000000000000'::uuid AS ns
),
     cats AS (
         -- (store_category_id, prefix, base_lat, base_lon)
         SELECT 1 AS cat_id, '한식' AS prefix,   37.5725::float8 AS lat, 126.9769::float8 AS lon UNION ALL
         SELECT 2,             '분식',            37.5555,                126.9369           UNION ALL
         SELECT 3,             '중식',            37.5665,                126.9780           UNION ALL
         SELECT 4,             '양식',            37.4979,                127.0276
     ),
     gen AS (
         -- 카테고리별 매장명 10개 생성 (한식_01 ~ 양식_10)
         SELECT
             c.cat_id,
             c.prefix,
             lpad(g.i::text, 2, '0') AS num_txt,
             (c.prefix || '_' || lpad(g.i::text,2,'0')) AS store_name,
             (c.lat + (random()-0.5)*0.01)  AS store_lat,
             (c.lon + (random()-0.5)*0.01)  AS store_lon
         FROM cats c
                  CROSS JOIN generate_series(1,10) AS g(i)
     )
INSERT INTO p_stores (
    store_id, application_id, manager_id,
    store_name, store_address, phone_number,
    store_category_id, min_cost, description,
    store_lat, store_lon, open_status, open_time, close_time, location,
    rating_sum, rating_count, avg_rating,
    created_at, created_by, updated_at, updated_by,
    deleted_at, deleted_by
)
SELECT
    uuid_generate_v5(ns.ns, g.store_name)                           AS store_id,       -- v5: 이름기반, 두 DB에서 동일
    gen_random_uuid()                                               AS application_id,
    gen_random_uuid()                                               AS manager_id,
    g.store_name,
    (g.prefix || '구 ' || g.num_txt || '번지')                      AS store_address,
    ('02-' || (3000 + (random()*6999)::int)::text || '-' ||
     lpad(((random()*9999)::int)::text,4,'0'))               AS phone_number,
    g.cat_id                                                        AS store_category_id,
    (CASE g.cat_id WHEN 2 THEN 8000 WHEN 4 THEN 15000 ELSE 10000 END) AS min_cost,
    (g.prefix || ' 카테고리 테스트 매장 ' || g.num_txt)            AS description,
    g.store_lat, g.store_lon,
    TRUE                                                            AS open_status,
    '09:00'::time                                                   AS open_time,
    '22:00'::time                                                   AS close_time,
    ST_SetSRID(ST_MakePoint(g.store_lon, g.store_lat),4326)::geography AS location,
    0::numeric                                                      AS rating_sum,
    0::int                                                          AS rating_count,
    0::numeric                                                      AS avg_rating,
    now(), 'system', now(), 'system',
    NULL, NULL
FROM gen g, ns
ON CONFLICT (store_id) DO NOTHING;   -- 재실행 안전

-- 메뉴 샘플 (대표 매장 3곳에만)
WITH ns AS (SELECT '00000000-0000-0000-0000-000000000000'::uuid AS ns)
INSERT INTO p_menus (
    menu_id, store_id, menu_num, menu_name, menu_category_code,
    price, description, is_available,
    is_unlimited, stock_quantity,
    created_by, updated_by
)
VALUES
    (uuid_generate_v5((SELECT ns FROM ns), '한식_01:menu:비빔밥'),
     (SELECT store_id FROM p_stores WHERE store_name='한식_01'),
     1, '비빔밥','BIBIMBAP', 9000, '신선한 야채와 고추장 비빔밥', TRUE,
     FALSE, 100,'system','system'),

    (uuid_generate_v5((SELECT ns FROM ns), '분식_01:menu:떡볶이'),
     (SELECT store_id FROM p_stores WHERE store_name='분식_01'),
     1, '떡볶이','TTEOKBOKKI', 6000, '매콤달콤한 떡볶이', TRUE,
     FALSE, 200,'system','system'),

    (uuid_generate_v5((SELECT ns FROM ns), '분식_01:menu:김밥'),
     (SELECT store_id FROM p_stores WHERE store_name='분식_01'),
     2, '김밥','GIMBAP', 4000, '정통 김밥', TRUE,
     TRUE, 0,'system','system'),

    (uuid_generate_v5((SELECT ns FROM ns), '양식_01:menu:스파게티'),
     (SELECT store_id FROM p_stores WHERE store_name='양식_01'),
     1, '스파게티','PASTA', 12000, '토마토 소스 스파게티', TRUE,
     FALSE, 50,'system','system')
ON CONFLICT (menu_id) DO NOTHING;

-- 배달 지역
INSERT INTO delivery_areas (area_id, area_name, created_by, updated_by) VALUES
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa','종로구','system','system'),
                                                                            ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb','마포구','system','system'),
                                                                            ('cccccccc-cccc-cccc-cccc-cccccccccccc','강남구','system','system')
ON CONFLICT DO NOTHING;

-- 매장-배달지역 매핑
INSERT INTO p_store_delivery_areas (store_id, area_id, delivery_fee, created_by, updated_by)
VALUES
    ((SELECT store_id FROM p_stores WHERE store_name='한식_01'),'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',2000,'system','system'),
    ((SELECT store_id FROM p_stores WHERE store_name='분식_01'),'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',1500,'system','system'),
    ((SELECT store_id FROM p_stores WHERE store_name='양식_01'),'cccccccc-cccc-cccc-cccc-cccccccccccc',3000,'system','system')
ON CONFLICT DO NOTHING;

-- AI 응답 샘플
INSERT INTO p_ai_responses (ai_response_id, description, created_by, updated_by)
VALUES ('dddddddd-dddd-dddd-dddd-dddddddddddd','메뉴 설명 자동 생성 결과 샘플','system','system')
ON CONFLICT (ai_response_id) DO NOTHING;
