CREATE VIEW vw_stock_position AS
SELECT
    s.id as stock_id,
    p.name as property_name,
    i.name as input_name,
    ic.name as category_name,
    s.stock_type,
    s.current_quantity,
    s.reserved_quantity,
    (s.current_quantity - s.reserved_quantity) as available_quantity,
    s.weighted_average_cost,
    s.total_value,
    i.minimum_stock,
    i.maximum_stock,
    CASE
        WHEN s.current_quantity <= i.minimum_stock THEN 'LOW'
        WHEN s.current_quantity >= i.maximum_stock THEN 'HIGH'
        ELSE 'NORMAL'
        END as stock_alert,
    w.name as warehouse_name,
    s.last_entry_date,
    s.last_exit_date
FROM stocks s
         JOIN properties p ON p.id = s.property_id
         LEFT JOIN inputs i ON i.id = s.input_id
         LEFT JOIN input_categories ic ON ic.id = i.category_id
         LEFT JOIN warehouses w ON w.id = s.warehouse_id
WHERE s.current_quantity > 0;

CREATE VIEW vw_batch_expiration AS
SELECT
    b.id as batch_id,
    b.batch_number,
    i.name as input_name,
    ic.name as category_name,
    p.name as property_name,
    s.name as supplier_name,
    b.current_quantity,
    b.expiration_date,
    b.unit_price,
    (b.current_quantity * b.unit_price) as current_value,
    (b.expiration_date - CURRENT_DATE) as days_until_expiration,
    CASE
        WHEN b.expiration_date < CURRENT_DATE THEN 'EXPIRED'
        WHEN b.expiration_date <= CURRENT_DATE + 15 THEN 'CRITICAL'
        WHEN b.expiration_date <= CURRENT_DATE + 30 THEN 'WARNING'
        ELSE 'OK'
        END as expiration_status
FROM batchs b
         JOIN inputs i ON i.id = b.input_id
         JOIN input_categories ic ON ic.id = i.category_id
         JOIN properties p ON p.id = b.property_id
         JOIN suppliers s ON s.id = b.supplier_id
WHERE b.status = 'ACTIVE'
  AND b.receipt_status = 'RECEIVED'
ORDER BY b.expiration_date ASC;

CREATE VIEW vw_stock_movements AS
SELECT
    sm.id as movement_id,
    sm.movement_type,
    sm.quantity,
    sm.unit_value,
    sm.total_value,
    sm.movement_date,
    p.name as property_name,
    i.name as input_name,
    u.name as user_name,
    b.batch_number,
    sm.notes,
    sm.reversed,
    sm.reversed_movement_id
FROM movement_stocks sm
         JOIN stocks s ON s.id = sm.stock_id
         JOIN properties p ON p.id = sm.property_id
         LEFT JOIN inputs i ON i.id = s.input_id
         JOIN users u ON u.id = sm.user_id
         LEFT JOIN batchs b ON b.id = sm.batch_id
ORDER BY sm.movement_date DESC;