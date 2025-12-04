CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    hotel_id BIGINT,
    customer_id BIGINT,
    amount NUMERIC(19,2),
    currency VARCHAR(10),
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_orders (
    id SERIAL PRIMARY KEY,
    booking_id BIGINT,
    amount NUMERIC(19,2),
    currency VARCHAR(10),
    payment_method VARCHAR(50),
    status VARCHAR(50),
    external_pg_order_id VARCHAR(255),
    external_pg_payment_id VARCHAR(255),
    idempotency_key VARCHAR(255),
    return_url VARCHAR(255),
    type VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_orders_idempotency ON payment_orders(idempotency_key);

CREATE TABLE IF NOT EXISTS payment_transactions (
    id SERIAL PRIMARY KEY,
    payment_order_id BIGINT,
    amount NUMERIC(19,2),
    currency VARCHAR(10),
    gateway_provider VARCHAR(50),
    external_transaction_id VARCHAR(255),
    status VARCHAR(50),
    error_code VARCHAR(50),
    error_message VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refunds (
    id SERIAL PRIMARY KEY,
    payment_order_id BIGINT,
    amount NUMERIC(19,2),
    reason VARCHAR(255),
    status VARCHAR(50),
    external_pg_refund_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id SERIAL PRIMARY KEY,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    debit_account VARCHAR(100),
    credit_account VARCHAR(100),
    amount NUMERIC(19,2),
    currency VARCHAR(10),
    narration VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hotel_accounts (
    id SERIAL PRIMARY KEY,
    hotel_id BIGINT,
    payable_balance NUMERIC(19,2)
);

CREATE TABLE IF NOT EXISTS settlements (
    id SERIAL PRIMARY KEY,
    hotel_id BIGINT,
    amount NUMERIC(19,2),
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    external_payout_reference VARCHAR(255)
);
