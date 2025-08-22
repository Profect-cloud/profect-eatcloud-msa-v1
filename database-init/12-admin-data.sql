\c admin_db;

-- admin-service/db/data.sql
-- 확장 (필요 시)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

--------------------------------------
-- 1) 상위 카테고리 p_store_categories
--------------------------------------
INSERT INTO p_store_categories (
    name, code, sort_order,
    is_active, total_store_amount,
    created_at, created_by, updated_at, updated_by
)
VALUES
    ('한식',   'KOREAN',  1, TRUE, 0, now(), 'system', now(), 'system'),
    ('분식',   'BUNSIK',  2, TRUE, 0, now(), 'system', now(), 'system'),
    ('중식',   'CHINESE', 3, TRUE, 0, now(), 'system', now(), 'system'),
    ('양식',   'WESTERN', 4, TRUE, 0, now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;

--------------------------------------
-- 2) 중간 카테고리 p_mid_categories
--    (INSERT ... SELECT 로 FK 안전 매핑)
--------------------------------------
-- 한식/밥류
INSERT INTO p_mid_categories (
    store_category_id, name, code, sort_order,
    is_active, total_store_amount,
    created_at, created_by, updated_at, updated_by
)
SELECT s.id, '밥류', 'RICE', 1,
       TRUE, 0, now(), 'system', now(), 'system'
FROM p_store_categories s
WHERE s.code = 'KOREAN'
ON CONFLICT (code) DO NOTHING;

-- 한식/면류
INSERT INTO p_mid_categories (
    store_category_id, name, code, sort_order,
    is_active, total_store_amount,
    created_at, created_by, updated_at, updated_by
)
SELECT s.id, '면류', 'NOODLE', 2,
       TRUE, 0, now(), 'system', now(), 'system'
FROM p_store_categories s
WHERE s.code = 'KOREAN'
ON CONFLICT (code) DO NOTHING;

-- 중식/볶음류
INSERT INTO p_mid_categories (
    store_category_id, name, code, sort_order,
    is_active, total_store_amount,
    created_at, created_by, updated_at, updated_by
)
SELECT s.id, '볶음류', 'FRIED', 1,
       TRUE, 0, now(), 'system', now(), 'system'
FROM p_store_categories s
WHERE s.code = 'CHINESE'
ON CONFLICT (code) DO NOTHING;

--------------------------------------
-- 3) 메뉴 카테고리 p_menu_categories
--------------------------------------
-- 한식/밥류 → 비빔밥
INSERT INTO p_menu_categories (
    store_category_id, mid_category_id, name, code, sort_order,
    is_active, total_store_amount,
    created_at, created_by, updated_at, updated_by
)
SELECT s.id, m.id, '비빔밥', 'BIBIMBAP', 1,
       TRUE, 0, now(), 'system', now(), 'system'
FROM p_store_categories s
         JOIN p_mid_categories m ON m.code = 'RICE'
WHERE s.code = 'KOREAN'
ON CONFLICT (code) DO NOTHING;

-- 한식/면류 → 칼국수
INSERT INTO p_menu_categories (
    store_category_id, mid_category_id, name, code, sort_order,
    is_active, total_store_amount,
    created_at, created_by, updated_at, updated_by
)
SELECT s.id, m.id, '칼국수', 'KALGUKSU', 2,
       TRUE, 0, now(), 'system', now(), 'system'
FROM p_store_categories s
         JOIN p_mid_categories m ON m.code = 'NOODLE'
WHERE s.code = 'KOREAN'
ON CONFLICT (code) DO NOTHING;

-- 중식/볶음류 → 짜장면
INSERT INTO p_menu_categories (
    store_category_id, mid_category_id, name, code, sort_order,
    is_active, total_store_amount,
    created_at, created_by, updated_at, updated_by
)
SELECT s.id, m.id, '짜장면', 'JJAJANG', 1,
       TRUE, 0, now(), 'system', now(), 'system'
FROM p_store_categories s
         JOIN p_mid_categories m ON m.code = 'FRIED'
WHERE s.code = 'CHINESE'
ON CONFLICT (code) DO NOTHING;

--------------------------------------
-- 4) 관리자 p_admins
--------------------------------------
INSERT INTO p_admins (
    id, name, email, password, phone_number, position,
    created_at, created_by, updated_at, updated_by
)
VALUES
    (gen_random_uuid(), '관리자1', 'admin1@example.com', 'adminpw1', '010-1111-2222', 'MASTER',
     now(), 'system', now(), 'system'),
    (gen_random_uuid(), '관리자2', 'admin2@example.com', 'adminpw2', '010-3333-4444', 'REVIEWER',
     now(), 'system', now(), 'system')
ON CONFLICT (email) DO NOTHING;

--------------------------------------
-- 5) 매니저 가게 신청 p_manager_store_applications
--------------------------------------
-- 홍길동 (PENDING)
INSERT INTO p_manager_store_applications (
    application_id,
    manager_name, manager_email, manager_password, manager_phone_number,
    store_name, store_address, store_phone_number, store_category_id,
    description, status, reviewer_admin_id, review_comment,
    created_at, created_by, updated_at, updated_by
)
SELECT
    gen_random_uuid(),
    '홍길동','hong@example.com','pw1234','010-5555-6666',
    '길동네 한식당','서울 종로구 1번지','02-111-2222',
    s.id,
    '정통 한식 전문점입니다.','PENDING', NULL, NULL,
    now(),'system',now(),'system'
FROM p_store_categories s
WHERE s.code = 'KOREAN'
ON CONFLICT DO NOTHING;

-- 김철수 (APPROVED, reviewer = admin2@example.com)
INSERT INTO p_manager_store_applications (
    application_id,
    manager_name, manager_email, manager_password, manager_phone_number,
    store_name, store_address, store_phone_number, store_category_id,
    description, status, reviewer_admin_id, review_comment,
    created_at, created_by, updated_at, updated_by
)
SELECT
    gen_random_uuid(),
    '김철수','kim@example.com','pw5678','010-7777-8888',
    '철수네 분식','서울 마포구 2번지','02-333-4444',
    s.id,
    '분식 전문점, 떡볶이/순대/튀김','APPROVED', a.id, '검토 완료 - 승인합니다.',
    now(),'system',now(),'system'
FROM p_store_categories s
         LEFT JOIN p_admins a ON a.email = 'admin2@example.com'
WHERE s.code = 'BUNSIK'
ON CONFLICT DO NOTHING;
