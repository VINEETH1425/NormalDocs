-- StoreConnect Omnichannel Retail Platform
-- PostgreSQL Database Initialization Script
-- Note: Run this script as the 'postgres' superuser.

-- 1. Create database (Execute separately in psql / pgAdmin if running inside transaction):
-- CREATE DATABASE storeconnect;

-- Connect to storeconnect database before executing below:
-- \c storeconnect;

-- 2. Create microservice isolated schemas
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS inventory_schema;
CREATE SCHEMA IF NOT EXISTS order_schema;
CREATE SCHEMA IF NOT EXISTS payment_schema;

COMMENT ON SCHEMA user_schema IS 'Customer authentication, profile, and active shopping cart data';
COMMENT ON SCHEMA inventory_schema IS 'Product catalog, physical retail stores, and multi-location inventory';
COMMENT ON SCHEMA order_schema IS 'Orders, order items, status tracking, and fulfillment lifecycle';
COMMENT ON SCHEMA payment_schema IS 'Payment transaction ledger and gateway audit history';
