\c order_db;

-- TimeData 테이블
CREATE TABLE IF NOT EXISTS p_time (
    p_time_id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

-- Order Status Code 테이블
CREATE TABLE IF NOT EXISTS order_status_codes (
    code VARCHAR(30) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    p_time_id UUID NOT NULL,
    FOREIGN KEY (p_time_id) REFERENCES p_time(p_time_id)
);

-- Order Type Code 테이블
CREATE TABLE IF NOT EXISTS order_type_codes (
    code VARCHAR(30) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    p_time_id UUID NOT NULL,
    FOREIGN KEY (p_time_id) REFERENCES p_time(p_time_id)
);

-- Orders 테이블
CREATE TABLE IF NOT EXISTS p_orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) NOT NULL UNIQUE,
    order_menu_list JSONB NOT NULL,
    customer_id UUID NOT NULL,
    store_id UUID NOT NULL,
    payment_id UUID,
    order_status VARCHAR(30) NOT NULL,
    order_type VARCHAR(30) NOT NULL,
    total_price INTEGER NOT NULL,
    use_points BOOLEAN NOT NULL DEFAULT FALSE,
    points_to_use INTEGER NOT NULL DEFAULT 0,
    final_payment_amount INTEGER NOT NULL,
    p_time_id UUID NOT NULL,
    FOREIGN KEY (p_time_id) REFERENCES p_time(p_time_id),
    FOREIGN KEY (order_status) REFERENCES order_status_codes(code),
    FOREIGN KEY (order_type) REFERENCES order_type_codes(code)
);

-- Delivery Orders 테이블
CREATE TABLE IF NOT EXISTS p_delivery_orders (
    order_id UUID PRIMARY KEY,
    delivery_fee DECIMAL(10, 2) DEFAULT 0,
    delivery_requests TEXT,
    zipcode VARCHAR(10),
    road_addr VARCHAR(500),
    detail_addr VARCHAR(200),
    estimated_delivery_time TIMESTAMP,
    estimated_preparation_time INTEGER,
    canceled_at TIMESTAMP,
    canceled_by VARCHAR(100),
    cancel_reason TEXT,
    p_time_id UUID NOT NULL,
    FOREIGN KEY (order_id) REFERENCES p_orders(order_id),
    FOREIGN KEY (p_time_id) REFERENCES p_time(p_time_id)
);

-- Pickup Orders 테이블
CREATE TABLE IF NOT EXISTS p_pickup_orders (
    order_id UUID PRIMARY KEY,
    pickup_requests TEXT,
    estimated_pickup_time TIMESTAMP,
    canceled_at TIMESTAMP,
    canceled_by VARCHAR(100),
    cancel_reason TEXT,
    p_time_id UUID NOT NULL,
    FOREIGN KEY (order_id) REFERENCES p_orders(order_id),
    FOREIGN KEY (p_time_id) REFERENCES p_time(p_time_id)
);

-- Reviews 테이블
CREATE TABLE IF NOT EXISTS p_reviews (
    review_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    rating DECIMAL(2, 1) NOT NULL,
    content TEXT NOT NULL,
    p_time_id UUID NOT NULL,
    FOREIGN KEY (order_id) REFERENCES p_orders(order_id),
    FOREIGN KEY (p_time_id) REFERENCES p_time(p_time_id)
);

-- Cart 테이블
CREATE TABLE IF NOT EXISTS p_cart (
    cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE,
    cart_items JSONB NOT NULL,
    p_time_id UUID NOT NULL,
    FOREIGN KEY (p_time_id) REFERENCES p_time(p_time_id)
);

-- 인덱스 생성
CREATE INDEX idx_orders_customer_id ON p_orders(customer_id);
CREATE INDEX idx_orders_store_id ON p_orders(store_id);
CREATE INDEX idx_orders_order_number ON p_orders(order_number);
CREATE INDEX idx_orders_payment_id ON p_orders(payment_id);
CREATE INDEX idx_reviews_order_id ON p_reviews(order_id);
CREATE INDEX idx_cart_customer_id ON p_cart(customer_id);
CREATE INDEX idx_time_deleted_at ON p_time(deleted_at);

