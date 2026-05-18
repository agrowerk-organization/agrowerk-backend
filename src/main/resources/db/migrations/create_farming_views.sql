CREATE VIEW vw_active_plantings AS
SELECT
    p.id as planting_id,
    pr.id as property_id,
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
    pr.id as property_id
    pr.name as property_name,
    AVG(y.productivity_per_hectare) as avg_productivity,
    SUM(y.total_produced_kg) as total_produced_kg,
    COUNT(h.id) as total_harvests
FROM fields f
         JOIN yields y ON y.field_id = f.id
         JOIN harvests h ON h.id = y.harvest_id
         JOIN plantings pl ON pl.id = h.planting_id
         JOIN properties pr ON pr.id = pl.property_id
GROUP BY f.id, f.name, pr.id, pr.name;

CREATE VIEW harvest_dashboard_view AS
SELECT
    h.planting_id,
    p.property_id,
    COUNT(hp.id)           AS total_partials,
    SUM(hp.quantity_kg)    AS total_harvested_kg,
    h.quality_grade,
    h.finalized,
    h.harvest_date,
    hf.estimated_quantity,
    hf.committed_quantity,
    hf.confidence_level,
    cv.name         AS variety_name,
    c.name          AS crop_name,
    f.name          AS field_name,
    s.name          AS season_name,
    p.planting_date,
    p.expected_harvest_date,
    CASE
        WHEN hf.estimated_quantity > 0
            THEN ROUND((SUM(hp.quantity_kg) / hf.estimated_quantity) * 100, 2)
        ELSE 0
        END AS achievement_rate,
    (hf.estimated_quantity - hf.committed_quantity) AS available_quantity
FROM harvests h
         LEFT JOIN harvest_partials hp ON hp.harvest_id = h.id
         LEFT JOIN plantings p ON p.id = h.planting_id
         LEFT JOIN fields f ON f.id = p.field_id
         LEFT JOIN seasons s ON s.id = p.season_id
         LEFT JOIN crops c ON c.id = p.crop_id
         LEFT JOIN crop_varieties cv ON cv.id = p.crop_variety_id
         LEFT JOIN harvest_forecasts hf ON hf.planting_id = h.planting_id
GROUP BY
    h.id, h.planting_id, p.property_id, h.quality_grade, h.finalized,
    h.harvest_date, hf.id, hf.estimated_quantity, hf.committed_quantity,
    hf.confidence_level, cv.name, c.name, f.name, s.name,
    p.planting_date, p.expected_harvest_date;