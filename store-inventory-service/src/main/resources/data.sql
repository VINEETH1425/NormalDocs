-- Categories
INSERT INTO inventory_schema.categories (id, name, description) VALUES
('CAT-AUDIO', 'Audio & Sound', 'Noise-cancelling headphones, wireless earphones, and portable speakers'),
('CAT-WEAR',  'Wearables & Smart Tech', 'Smartwatches, fitness bands, and wearable health trackers'),
('CAT-COMP',  'Computing & Accessories', 'High-productivity monitors, mechanical keyboards, and precision mice'),
('CAT-LIFE',  'Lifestyle & Essentials', 'Performance athletic wear, ergonomic backpacks, and specialty coffee')
ON CONFLICT (id) DO NOTHING;

-- Stores
INSERT INTO inventory_schema.stores (id, name, code, address, city, phone, is_active) VALUES
('STORE-GGN-01', 'StoreConnect CyberHub',      'STR-CH-01',  'DLF CyberHub, DLF Phase 2, Sector 24',   'Gurugram',    '+91 124 4567890', TRUE),
('STORE-GGN-02', 'StoreConnect Ambience',      'STR-AMB-02', 'Ambience Mall, NH-8, Ambience Island',   'Gurugram',    '+91 124 4987654', TRUE),
('STORE-DEL-01', 'StoreConnect CP Flagship',   'STR-CP-03',  'Inner Circle, Block B, Connaught Place', 'New Delhi',   '+91 11 23456789', TRUE),
('STORE-DEL-02', 'StoreConnect Select Citywalk','STR-SCW-04', 'Select Citywalk, A-3 District Centre, Saket', 'New Delhi', '+91 11 41234567', TRUE),
('STORE-NOI-01', 'StoreConnect Mall of India', 'STR-MOI-05', 'Sector 18, Noida',                       'Noida',       '+91 120 6789012', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Products
INSERT INTO inventory_schema.products (id, sku, name, description, category_id, price, is_active) VALUES
('PROD-AUD-001', 'SKU-SNY-WH1000XM5', 'Sony WH-1000XM5 ANC Headphones', 'Industry-leading noise canceling with Auto NC Optimizer', 'CAT-AUDIO', 29990.00, TRUE),
('PROD-AUD-002', 'SKU-APL-APP2',      'Apple AirPods Pro (2nd Gen)',     'Active Noise Cancellation, Adaptive Audio, USB-C case',   'CAT-AUDIO', 24900.00, TRUE),
('PROD-AUD-003', 'SKU-BOSE-QC45',     'Bose QuietComfort 45',            'Iconic quiet, comfort, and crisp balanced sound',          'CAT-AUDIO', 26900.00, TRUE),
('PROD-WEAR-001','SKU-APL-AW9',       'Apple Watch Series 9 GPS 45mm',   'Advanced health sensors, bright Always-On Retina display', 'CAT-WEAR',  44900.00, TRUE),
('PROD-WEAR-002','SKU-GAR-FR265',     'Garmin Forerunner 265 GPS Watch', 'Brilliant AMOLED touchscreen display with training metrics', 'CAT-WEAR', 41990.00, TRUE),
('PROD-WEAR-003','SKU-SAM-GW6',       'Samsung Galaxy Watch6 Classic',   'Rotating bezel, sleep coaching, sapphire crystal glass',   'CAT-WEAR',  36999.00, TRUE),
('PROD-COMP-001','SKU-LOG-MXM3S',     'Logitech MX Master 3S Mouse',     'Quiet clicks, 8K DPI any-surface tracking, MagSpeed wheel','CAT-COMP',  8995.00,  TRUE),
('PROD-COMP-002','SKU-KEY-Q1P',       'Keychron Q1 Pro Mechanical Board','Wireless QMK/VIA custom mechanical keyboard, tactile brown','CAT-COMP', 18999.00, TRUE),
('PROD-COMP-003','SKU-DEL-U2723QE',   'Dell UltraSharp 27 4K USB-C Hub', 'IPS Black technology, 4K UHD, 98% DCI-P3 color gamut',     'CAT-COMP', 56990.00, TRUE),
('PROD-LIFE-001','SKU-PEAK-EDC20',    'Peak Design Everyday Backpack 20L','Versatile weatherproof commuter and camera gear backpack', 'CAT-LIFE', 25999.00, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Initial Inventory Stock
INSERT INTO inventory_schema.store_inventory (id, store_id, product_id, quantity_available, quantity_reserved, reorder_threshold) VALUES
('INV-001', 'STORE-GGN-01', 'PROD-AUD-001', 15, 0, 3),
('INV-002', 'STORE-GGN-01', 'PROD-AUD-002', 20, 0, 5),
('INV-003', 'STORE-GGN-01', 'PROD-WEAR-001', 10, 0, 2),
('INV-004', 'STORE-GGN-01', 'PROD-COMP-001', 25, 0, 5),
('INV-005', 'STORE-GGN-02', 'PROD-AUD-001', 8,  0, 3),
('INV-006', 'STORE-GGN-02', 'PROD-AUD-002', 12, 0, 4),
('INV-007', 'STORE-GGN-02', 'PROD-WEAR-002', 7,  0, 2),
('INV-008', 'STORE-DEL-01', 'PROD-AUD-001', 18, 0, 3),
('INV-009', 'STORE-DEL-01', 'PROD-AUD-003', 14, 0, 3),
('INV-010', 'STORE-DEL-01', 'PROD-COMP-002', 9,  0, 2),
('INV-011', 'STORE-DEL-01', 'PROD-COMP-003', 5,  0, 1),
('INV-012', 'STORE-DEL-02', 'PROD-WEAR-001', 11, 0, 2),
('INV-013', 'STORE-DEL-02', 'PROD-WEAR-003', 6,  0, 2),
('INV-014', 'STORE-DEL-02', 'PROD-LIFE-001', 15, 0, 3),
('INV-015', 'STORE-NOI-01', 'PROD-AUD-002', 16, 0, 4),
('INV-016', 'STORE-NOI-01', 'PROD-COMP-001', 22, 0, 5),
('INV-017', 'STORE-NOI-01', 'PROD-LIFE-001', 10, 0, 2)
ON CONFLICT (id) DO NOTHING;
