CREATE MATERIALIZED VIEW mv_season_dashboard AS
SELECT
    s.id as season_id,
    s.name as season_name,
    pr.id as property_id,
    pr.name as property_name,
    c.name as crop_name,
    COUNT(pl.id) as total_plantings,
    SUM(pl.area_hectares) as total_area,
    SUM(y.total_produced_kg) as total_produced_kg,
    AVG(y.productivity_per_hectare) as avg_productivity
FROM seasons s
         JOIN plantings pl ON pl.season_id = s.id
         JOIN properties pr ON pr.id = pl.property_id
         JOIN crops c ON c.id = pl.crop_id
         LEFT JOIN harvests h ON h.planting_id = pl.id
         LEFT JOIN yields y ON y.harvest_id = h.id
GROUP BY s.id, s.name, pr.id, pr.name, c.name
    WITH DATA;

CREATE INDEX idx_mv_season_property
    ON mv_season_dashboard(season_id, property_id);