-- store/data.sql
CREATE SCHEMA IF NOT EXISTS stores;
SET search_path TO stores;

-- UUID 함수 사용
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-----------------------------
-- 매장 (p_stores)
-----------------------------
INSERT INTO p_stores (
    store_id, application_id, manager_id,
    store_name, store_address, phone_number,
    store_category_id, min_cost, description, store_lat, store_lon,
    open_status, open_time, close_time, location,
    created_by, updated_by
)
VALUES
    (gen_random_uuid(), gen_random_uuid(), gen_random_uuid(),
     '길동네 한식당', '서울 종로구 1번지', '02-111-2222',
     1, 10000, '정통 한식 전문점입니다.', 37.5725, 126.9769,
     TRUE, '10:00', '22:00',
     ST_SetSRID(ST_MakePoint(126.9769, 37.5725), 4326)::geography,
     'system','system'),

    (gen_random_uuid(), gen_random_uuid(), gen_random_uuid(),
     '철수네 분식', '서울 마포구 2번지', '02-333-4444',
     2, 8000, '분식 전문점, 떡볶이/순대/튀김', 37.5555, 126.9369,
     TRUE, '09:00', '21:00',
     ST_SetSRID(ST_MakePoint(126.9369, 37.5555), 4326)::geography,
     'system','system'),

    (gen_random_uuid(), gen_random_uuid(), gen_random_uuid(),
     '마포 양식당', '서울 마포구 10번지', '02-888-9999',
     4, 15000, '파스타와 스테이크 전문점', 37.5560, 126.9400,
     FALSE, '11:00', '23:00',
     ST_SetSRID(ST_MakePoint(126.9400, 37.5560), 4326)::geography,
     'system','system');


-----------------------------
-- 메뉴 (p_menus)
-- store_id를 참조해야 해서 SELECT 서브쿼리로 연결
-----------------------------
INSERT INTO p_menus (
    menu_id, store_id, menu_num, menu_name, menu_category_code,
    price, description, is_available, created_by, updated_by
)
VALUES
    (gen_random_uuid(),
     (SELECT store_id FROM p_stores WHERE store_name='길동네 한식당'),
     1, '비빔밥', 'BIBIMBAP', 9000, '신선한 야채와 고추장 비빔밥', TRUE, 'system','system'),

    (gen_random_uuid(),
     (SELECT store_id FROM p_stores WHERE store_name='철수네 분식'),
     1, '떡볶이', 'TTEOKBOKKI', 6000, '매콤달콤한 떡볶이', TRUE, 'system','system'),

    (gen_random_uuid(),
     (SELECT store_id FROM p_stores WHERE store_name='철수네 분식'),
     2, '김밥', 'GIMBAP', 4000, '정통 김밥', TRUE, 'system','system'),

    (gen_random_uuid(),
     (SELECT store_id FROM p_stores WHERE store_name='마포 양식당'),
     1, '스파게티', 'PASTA', 12000, '토마토 소스 스파게티', TRUE, 'system','system');

-----------------------------
-- 배달 지역 (delivery_areas)
-----------------------------
INSERT INTO delivery_areas (area_id, area_name, created_by, updated_by)
VALUES
    (gen_random_uuid(), '종로구', 'system','system'),
    (gen_random_uuid(), '마포구', 'system','system'),
    (gen_random_uuid(), '강남구', 'system','system');

-----------------------------
-- 매장-배달지역 매핑 (p_store_delivery_areas)
-----------------------------
INSERT INTO p_store_delivery_areas (store_id, area_id, delivery_fee, created_by, updated_by)
VALUES
    ((SELECT store_id FROM p_stores WHERE store_name='길동네 한식당'),
     (SELECT area_id FROM delivery_areas WHERE area_name='종로구'), 2000, 'system','system'),

    ((SELECT store_id FROM p_stores WHERE store_name='철수네 분식'),
     (SELECT area_id FROM delivery_areas WHERE area_name='마포구'), 1500, 'system','system'),

    ((SELECT store_id FROM p_stores WHERE store_name='마포 양식당'),
     (SELECT area_id FROM delivery_areas WHERE area_name='강남구'), 3000, 'system','system');

-----------------------------
-- AI Response 샘플
-----------------------------
INSERT INTO p_ai_responses (ai_response_id, description, created_by, updated_by)
VALUES
    (gen_random_uuid(), '메뉴 설명 자동 생성 결과 샘플', 'system','system');

-----------------------------
-- 일별 매장 매출 (daily_store_sales)
-----------------------------
INSERT INTO daily_store_sales (
    sale_date, store_id, order_count, total_amount,
    created_by, updated_by
)
VALUES
    (CURRENT_DATE - INTERVAL '2 days',
     (SELECT store_id FROM p_stores WHERE store_name='길동네 한식당'),
     20, 180000, 'system','system'),

    (CURRENT_DATE - INTERVAL '1 days',
     (SELECT store_id FROM p_stores WHERE store_name='철수네 분식'),
     35, 220000, 'system','system'),

    (CURRENT_DATE,
     (SELECT store_id FROM p_stores WHERE store_name='마포 양식당'),
     10, 150000, 'system','system');

-----------------------------
-- 일별 메뉴 매출 (daily_menu_sales)
-----------------------------
INSERT INTO daily_menu_sales (
    sale_date, store_id, menu_id, quantity_sold, total_amount,
    created_by, updated_by
)
VALUES
    (CURRENT_DATE - INTERVAL '2 days',
     (SELECT store_id FROM p_stores WHERE store_name='길동네 한식당'),
     (SELECT menu_id FROM p_menus WHERE menu_name='비빔밥'),
     15, 135000, 'system','system'),

    (CURRENT_DATE - INTERVAL '1 days',
     (SELECT store_id FROM p_stores WHERE store_name='철수네 분식'),
     (SELECT menu_id FROM p_menus WHERE menu_name='떡볶이'),
     20, 120000, 'system','system'),

    (CURRENT_DATE - INTERVAL '1 days',
     (SELECT store_id FROM p_stores WHERE store_name='철수네 분식'),
     (SELECT menu_id FROM p_menus WHERE menu_name='김밥'),
     15, 60000, 'system','system'),

    (CURRENT_DATE,
     (SELECT store_id FROM p_stores WHERE store_name='마포 양식당'),
     (SELECT menu_id FROM p_menus WHERE menu_name='스파게티'),
     10, 120000, 'system','system');
