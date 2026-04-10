CREATE TABLE commodity_prices
(
    id             UUID           NOT NULL,
    commodity      VARCHAR(20)    NOT NULL,
    price          DECIMAL(12, 4) NOT NULL,
    unit           VARCHAR(30),
    region         VARCHAR(60),
    reference_date date           NOT NULL,
    fetched_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_commodity_prices PRIMARY KEY (id)
);

ALTER TABLE commodity_prices
    ADD CONSTRAINT uq_commodity_region_date UNIQUE (commodity, region, reference_date);

CREATE INDEX idx_commodity_date ON commodity_prices (commodity, reference_date DESC);

CREATE INDEX idx_commodity_region_date ON commodity_prices (commodity, region, reference_date DESC);

CREATE INDEX idx_reference_date ON commodity_prices (reference_date DESC);
