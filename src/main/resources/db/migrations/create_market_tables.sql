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

CREATE TABLE exchange_rates
(
    id             UUID          NOT NULL,
    currency_pair  VARCHAR(10)   NOT NULL,
    rate           DECIMAL(8, 4) NOT NULL,
    reference_date date          NOT NULL,
    CONSTRAINT pk_exchange_rates PRIMARY KEY (id)
);

ALTER TABLE exchange_rates
    ADD CONSTRAINT uc_fc9e3b2f429eb8a0dc726e2f6 UNIQUE (currency_pair, reference_date);

CREATE TABLE market_alerts
(
    id             UUID                        NOT NULL,
    commodity      VARCHAR(255)                NOT NULL,
    type           VARCHAR(255)                NOT NULL,
    message        VARCHAR(255)                NOT NULL,
    trigger_value  DECIMAL(10, 4),
    reference_date date                        NOT NULL,
    read           BOOLEAN                     NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_market_alerts PRIMARY KEY (id)
);

CREATE INDEX idx_alert_commodity_date ON market_alerts (commodity, reference_date);

CREATE INDEX idx_alert_read ON market_alerts (read);

CREATE TABLE market_reports
(
    id             UUID NOT NULL,
    report_type    VARCHAR(255),
    commodity      VARCHAR(255),
    period_start   date,
    period_end     date,
    summary        TEXT,
    report_payload JSONB,
    report_status  VARCHAR(255),
    generated_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_market_reports PRIMARY KEY (id)
);

CREATE INDEX idx_report_commodity_date ON market_reports (commodity, period_start);

CREATE INDEX idx_report_type_status ON market_reports (report_type, report_status);
