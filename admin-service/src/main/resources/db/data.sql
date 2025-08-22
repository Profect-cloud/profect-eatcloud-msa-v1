SET search_path TO admin;

-------------------------
-- 상위 카테고리 (Store Categories)
-------------------------
INSERT INTO p_store_categories (name, code, sort_order, created_by, updated_by)
VALUES
    ('한식',   'KOREAN',  1, 'system', 'system'),
    ('분식',   'BUNSIK',  2, 'system', 'system'),
    ('중식',   'CHINESE', 3, 'system', 'system'),
    ('양식',   'WESTERN', 4, 'system', 'system');

-------------------------
-- 중간 카테고리 (Mid Categories)
-------------------------
INSERT INTO p_mid_categories (store_category_id, name, code, sort_order, created_by, updated_by)
VALUES
    ((SELECT id FROM p_store_categories WHERE code='KOREAN'), '밥류',   'RICE',   1, 'system', 'system'),
    ((SELECT id FROM p_store_categories WHERE code='KOREAN'), '면류',   'NOODLE', 2, 'system', 'system'),
    ((SELECT id FROM p_store_categories WHERE code='CHINESE'), '볶음류', 'FRIED',  1, 'system', 'system');

-------------------------
-- 메뉴 카테고리 (Menu Categories)
-------------------------
INSERT INTO p_menu_categories (store_category_id, mid_category_id, name, code, sort_order, created_by, updated_by)
VALUES
    ((SELECT id FROM p_store_categories WHERE code='KOREAN'),
     (SELECT id FROM p_mid_categories WHERE code='RICE'),
     '비빔밥', 'BIBIMBAP', 1, 'system', 'system'),

    ((SELECT id FROM p_store_categories WHERE code='KOREAN'),
     (SELECT id FROM p_mid_categories WHERE code='NOODLE'),
     '칼국수', 'KALGUKSU', 2, 'system', 'system'),

    ((SELECT id FROM p_store_categories WHERE code='CHINESE'),
     (SELECT id FROM p_mid_categories WHERE code='FRIED'),
     '짜장면', 'JJAJANG', 1, 'system', 'system');

-------------------------
-- 관리자(Admins)
-------------------------
INSERT INTO p_admins (id, name, email, password, phone_number, position, created_by, updated_by)
VALUES
    (gen_random_uuid(), '관리자1', 'admin1@example.com', 'adminpw1', '010-1111-2222', 'MASTER', 'system', 'system'),
    (gen_random_uuid(), '관리자2', 'admin2@example.com', 'adminpw2', '010-3333-4444', 'REVIEWER', 'system', 'system');

-------------------------
-- 매니저 가게 신청 (Manager Store Applications)
-------------------------
INSERT INTO p_manager_store_applications (
    manager_name, manager_email, manager_password, manager_phone_number,
    store_name, store_address, store_phone_number, store_category_id,
    description, status, reviewer_admin_id, review_comment
)
VALUES
    ('홍길동', 'hong@example.com', 'pw1234', '010-5555-6666',
     '길동네 한식당', '서울 종로구 1번지', '02-111-2222',
     (SELECT id FROM p_store_categories WHERE code='KOREAN'),
     '정통 한식 전문점입니다.', 'PENDING', NULL, NULL),

    ('김철수', 'kim@example.com', 'pw5678', '010-7777-8888',
     '철수네 분식', '서울 마포구 2번지', '02-333-4444',
     (SELECT id FROM p_store_categories WHERE code='BUNSIK'),
     '분식 전문점, 떡볶이/순대/튀김', 'APPROVED',
     (SELECT id FROM p_admins WHERE email='admin2@example.com' LIMIT 1),
     '검토 완료 - 승인합니다.');
