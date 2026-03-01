CREATE VIEW vw_active_plantings AS
SELECT
    p.id as planting_id,
    pr.name as property_name,
    c.name as crop_name,
    s.name as season_name,
    f.name as field_name,
    p.area_hectares,
    p.planting_date,
    p.expected_harvest_date,
    p.planting_status
FROM plantings p
         JOIN properties pr ON pr.id = p.property_id
         JOIN crops c ON c.id = p.crop_id
         JOIN seasons s ON s.id = p.season_id
         JOIN fields f ON f.id = p.field_id
WHERE p.planting_status = 'ACTIVE';

CREATE VIEW vw_field_productivity AS
SELECT
    f.id as field_id,
    f.name as field_name,
    pr.name as property_name,
    AVG(y.productivity_per_hectare) as avg_productivity,
    SUM(y.total_produced_kg) as total_produced_kg,
    COUNT(h.id) as total_harvests
FROM fields f
         JOIN yields y ON y.field_id = f.id
         JOIN harvests h ON h.id = y.harvest_id
         JOIN plantings pl ON pl.id = h.planting_id
         JOIN properties pr ON pr.id = pl.property_id
GROUP BY f.id, f.name, pr.name;

