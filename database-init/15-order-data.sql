\c order_db;

-- ================================
-- order-db/init/data.sql (FDW 없는 버전)
-- ================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 상태 코드
INSERT INTO order_status_codes (code, display_name, sort_order, is_active, created_at, created_by, updated_at, updated_by)
VALUES
    ('PENDING','대기중',1,TRUE,now(),'system',now(),'system'),
    ('PAID','결제완료',2,TRUE,now(),'system',now(),'system'),
    ('COMPLETED','완료',3,TRUE,now(),'system',now(),'system')
ON CONFLICT (code) DO NOTHING;

-- 타입 코드
INSERT INTO order_type_codes (code, display_name, sort_order, is_active, created_at, created_by, updated_at, updated_by)
VALUES
    ('DELIVERY','배달',1,TRUE,now(),'system',now(),'system'),
    ('PICKUP','포장', 2,TRUE,now(),'system',now(),'system')
ON CONFLICT (code) DO NOTHING;

-- v5 네임스페이스(★ store-db와 동일 값 사용!)
WITH ns AS (SELECT '00000000-0000-0000-0000-000000000000'::uuid AS ns),
     stores AS (
         -- FDW 없이, 우리가 넣은 대표 매장명 4개로 store_id를 결정적으로 생성
         SELECT '한식_01' AS store_name, uuid_generate_v5(ns.ns, '한식_01') AS store_id UNION ALL
         SELECT '분식_01', uuid_generate_v5(ns.ns, '분식_01') UNION ALL
         SELECT '중식_01', uuid_generate_v5(ns.ns, '중식_01') UNION ALL
         SELECT '양식_01', uuid_generate_v5(ns.ns, '양식_01')
     ),
     orders_src AS (
         -- 매장별 20건 주문 생성 (v5로 order_id도 결정적 생성 → 멱등)
         SELECT
             s.store_name, s.store_id,
             i AS seq,
             uuid_generate_v5(ns.ns, s.store_name || ':order:' || i::text) AS order_id,
             'ORD-' || substr(md5(s.store_name || ':' || i::text),1,8) AS order_number,
             (CASE WHEN random() < 0.5 THEN 'DELIVERY' ELSE 'PICKUP' END) AS order_type,
             'COMPLETED' AS order_status,
             (10000 + (random()*40000)::int) AS total_price,
             now() - ((random()*7)::int || ' days')::interval AS created_at
         FROM stores s, ns
                            CROSS JOIN generate_series(1,20) AS g(i)
     )
INSERT INTO p_orders (
    order_id, order_number, customer_id, store_id, payment_id,
    order_status, order_type, order_menu_list,
    total_price, use_points, points_to_use, final_payment_amount,
    created_at, created_by, updated_at, updated_by
)
SELECT
    o.order_id, o.order_number,
    gen_random_uuid() AS customer_id,
    o.store_id, NULL::uuid,
    o.order_status, o.order_type, '[]'::jsonb,
    o.total_price, FALSE, 0, o.total_price,
    o.created_at, 'system', o.created_at, 'system'
FROM orders_src o
ON CONFLICT (order_id) DO NOTHING;

-- 리뷰: 주문당 2개(결정적 review_id) → FK 보장 + 재실행 멱등
WITH ns AS (SELECT '00000000-0000-0000-0000-000000000000'::uuid AS ns),
     recent_orders AS (
         SELECT o.order_id, s.store_name
         FROM p_orders o
                  JOIN LATERAL (
             -- store_name 을 v5 역추적할 수 없으니, 주문번호의 앞부분으로 store 구분이 필요하면
             -- orders_src 와 동일 규칙을 사용할 것. 여기서는 단순히 최근 7일 주문 전체 사용.
             SELECT 1
             ) x ON TRUE
         WHERE o.created_at > now() - interval '7 days'
     )
INSERT INTO p_reviews (
    review_id, order_id, rating, content,
    created_at, created_by, updated_at, updated_by
)
SELECT
    uuid_generate_v5(ns.ns, o.order_id::text || ':review:' || i::text) AS review_id,
    o.order_id,
    round(((random()*2)+3)::numeric,1),  -- 3.0~5.0
    '리뷰 내용 ' || i::text,
    now(), 'system', now(), 'system'
FROM recent_orders o, ns
                          CROSS JOIN generate_series(1,2) AS g(i)
ON CONFLICT (review_id) DO NOTHING;

