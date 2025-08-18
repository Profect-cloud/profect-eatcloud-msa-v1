-- PAYMENTS service

CREATE TABLE payment_status_codes (
  code         VARCHAR(30) PRIMARY KEY,
  display_name VARCHAR(50) NOT NULL,
  sort_order   INTEGER     NOT NULL,
  is_active    BOOLEAN     NOT NULL DEFAULT true,
  p_time_id    UUID        NOT NULL   -- FK 제거됨
);

CREATE TABLE payment_method_codes (
  code         VARCHAR(30) PRIMARY KEY,
  display_name VARCHAR(50) NOT NULL,
  sort_order   INTEGER     NOT NULL,
  is_active    BOOLEAN     NOT NULL DEFAULT true,
  p_time_id    UUID        NOT NULL   -- FK 제거됨
);

CREATE TABLE p_payment_requests (
  payment_request_id UUID PRIMARY KEY,
  order_id           UUID         NOT NULL, -- 논리참조: orders.p_orders.order_id
  pg_provider        VARCHAR(100) NOT NULL,
  request_payload    JSONB        NOT NULL,
  status             VARCHAR(50)  NOT NULL,
  requested_at       TIMESTAMP    NOT NULL,
  responded_at       TIMESTAMP,
  failure_reason     TEXT,
  p_time_id          UUID         NOT NULL   -- FK 제거됨
);

CREATE TABLE p_payments (
  payment_id           UUID PRIMARY KEY,
  customer_id          UUID        NOT NULL, -- 논리참조: users.p_customer.id
  payment_request_id   UUID        NOT NULL,
  payment_status       VARCHAR(30) NOT NULL,
  payment_method       VARCHAR(30) NOT NULL,
  total_amount         INTEGER     NOT NULL,
  pg_transaction_id    VARCHAR(100),
  approval_code        VARCHAR(50),
  card_info            JSONB,
  requested_at         TIMESTAMP,
  approved_at          TIMESTAMP,
  failed_at            TIMESTAMP,
  failure_reason       TEXT,
  offline_payment_note TEXT,
  p_time_id            UUID        NOT NULL   -- FK 제거됨
);
ALTER TABLE p_payments
  ADD CONSTRAINT fk_payments_pr FOREIGN KEY (payment_request_id) REFERENCES p_payment_requests (payment_request_id);
ALTER TABLE p_payments
  ADD CONSTRAINT fk_payments_ps FOREIGN KEY (payment_status) REFERENCES payment_status_codes (code);
ALTER TABLE p_payments
  ADD CONSTRAINT fk_payments_pm FOREIGN KEY (payment_method) REFERENCES payment_method_codes (code);
