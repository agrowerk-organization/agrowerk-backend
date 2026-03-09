CREATE TABLE audit_logs
(
    id           UUID                        NOT NULL,
    event_type   VARCHAR(50)                 NOT NULL,
    user_id      UUID,
    ip_address   VARCHAR(45),
    user_agent   VARCHAR(500),
    details      VARCHAR(1000),
    timestamp    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    geo_location VARCHAR(100),
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

CREATE INDEX idx_event_type ON audit_logs (event_type);

CREATE INDEX idx_ip_address ON audit_logs (ip_address);

CREATE INDEX idx_timestamp ON audit_logs (timestamp);

CREATE INDEX idx_user_id ON audit_logs (user_id);

CREATE TABLE barter_exchange_rates
(
    id                    UUID                        NOT NULL,
    crop_id               UUID                        NOT NULL,
    asset_category        VARCHAR(255)                NOT NULL,
    crop_price_per_kg     DECIMAL(10, 2)              NOT NULL,
    asset_reference_value DECIMAL(10, 2)              NOT NULL,
    exchange_rate         DECIMAL(10, 4)              NOT NULL,
    valid_from            date                        NOT NULL,
    valid_until           date                        NOT NULL,
    active                BOOLEAN                     NOT NULL,
    region                VARCHAR(255),
    notes                 TEXT,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_barter_exchange_rates PRIMARY KEY (id)
);

ALTER TABLE barter_exchange_rates
    ADD CONSTRAINT FK_BARTER_EXCHANGE_RATES_ON_CROP FOREIGN KEY (crop_id) REFERENCES crops (id);

