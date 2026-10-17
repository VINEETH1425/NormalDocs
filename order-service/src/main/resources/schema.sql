CREATE SCHEMA IF NOT EXISTS order_schema;

CREATE TABLE IF NOT EXISTS order_schema.orders (
    id VARCHAR(50) PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    store_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    fulfillment_type VARCHAR(30) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 CHECK (total_amount >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_schema.order_items (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES order_schema.orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_orders_customer ON order_schema.orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_store ON order_schema.orders(store_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON order_schema.orders(status);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_schema.order_items(order_id);
