CREATE TABLE agricultural_practices
(
    id                  UUID                        NOT NULL,
    practipe_type       VARCHAR(255)                NOT NULL,
    application_date    date                        NOT NULL,
    product_used        VARCHAR(200),
    quantity_used       DECIMAL(10, 2),
    unit_of_measure     VARCHAR(20),
    cost_amount         DECIMAL(10, 2),
    responsible_user_id UUID,
    observations        TEXT,
    planting_id         UUID                        NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_agricultural_practices PRIMARY KEY (id)
);

CREATE INDEX idx_practice_date ON agricultural_practices (application_date);

CREATE INDEX idx_practice_type ON agricultural_practices (practipe_type);

ALTER TABLE agricultural_practices
    ADD CONSTRAINT FK_AGRICULTURAL_PRACTICES_ON_PLANTING FOREIGN KEY (planting_id) REFERENCES plantings (id);

CREATE INDEX idx_practice_planting ON agricultural_practices (planting_id);

ALTER TABLE agricultural_practices
    ADD CONSTRAINT FK_AGRICULTURAL_PRACTICES_ON_RESPONSIBLE_USER FOREIGN KEY (responsible_user_id) REFERENCES users (id);

CREATE TABLE batchs
(
    id                 UUID                        NOT NULL,
    batch_number       VARCHAR(50)                 NOT NULL,
    invoice_number     VARCHAR(50),
    initial_quantity   DECIMAL(10, 3)              NOT NULL,
    current_quantity   DECIMAL(10, 3)              NOT NULL,
    manufacturing_date date                        NOT NULL,
    expiration_date    date                        NOT NULL,
    entry_date         date                        NOT NULL,
    unit_price         DECIMAL(10, 2)              NOT NULL,
    total_value        DECIMAL(10, 2)              NOT NULL,
    status             VARCHAR(30)                 NOT NULL,
    receipt_status     VARCHAR(255)                NOT NULL,
    property_id        UUID,
    received_at        TIMESTAMP WITHOUT TIME ZONE,
    received_by        UUID,
    notes              TEXT,
    input_id           UUID                        NOT NULL,
    supplier_id        UUID                        NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_batchs PRIMARY KEY (id)
);

ALTER TABLE batchs
    ADD CONSTRAINT uc_batchs_batchnumber UNIQUE (batch_number);

ALTER TABLE batchs
    ADD CONSTRAINT FK_BATCHS_ON_INPUT FOREIGN KEY (input_id) REFERENCES inputs (id);

ALTER TABLE batchs
    ADD CONSTRAINT FK_BATCHS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

ALTER TABLE batchs
    ADD CONSTRAINT FK_BATCHS_ON_RECEIVED_BY FOREIGN KEY (received_by) REFERENCES users (id);

ALTER TABLE batchs
    ADD CONSTRAINT FK_BATCHS_ON_SUPPLIER FOREIGN KEY (supplier_id) REFERENCES suppliers (id);

CREATE TABLE crops
(
    id                UUID                        NOT NULL,
    name              VARCHAR(255)                NOT NULL,
    scientific_name   VARCHAR(255)                NOT NULL,
    growth_cycle_days INTEGER                     NOT NULL,
    crop_category     VARCHAR(255)                NOT NULL,
    created_by        UUID                        NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_crops PRIMARY KEY (id)
);

ALTER TABLE crops
    ADD CONSTRAINT FK_CROPS_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES users (id);

CREATE TABLE crop_varieties
(
    id          UUID                        NOT NULL,
    name        VARCHAR(100)                NOT NULL,
    description VARCHAR(200),
    region      VARCHAR(255),
    crop_id     UUID                        NOT NULL,
    created_by  UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_crop_varieties PRIMARY KEY (id)
);

ALTER TABLE crop_varieties
    ADD CONSTRAINT FK_CROP_VARIETIES_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES users (id);

ALTER TABLE crop_varieties
    ADD CONSTRAINT FK_CROP_VARIETIES_ON_CROP FOREIGN KEY (crop_id) REFERENCES crops (id);

CREATE TABLE fields
(
    id               UUID                        NOT NULL,
    name             VARCHAR(100)                NOT NULL,
    code             VARCHAR(20),
    area_hectares    DECIMAL(10, 2),
    description      TEXT,
    soil_type        VARCHAR(255)                NOT NULL,
    field_status     VARCHAR(255)                NOT NULL,
    slope_percentage DECIMAL(5, 2),
    notes            TEXT,
    property_id      UUID                        NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    geo_latitude     DECIMAL,
    geo_longitude    DECIMAL,
    CONSTRAINT pk_fields PRIMARY KEY (id)
);

CREATE INDEX idx_field_status ON fields (field_status);

ALTER TABLE fields
    ADD CONSTRAINT FK_FIELDS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

CREATE INDEX idx_field_property ON fields (property_id);

CREATE TABLE harvests
(
    id            UUID                        NOT NULL,
    harvest_date  date                        NOT NULL,
    finalized_at  date,
    quality_grade VARCHAR(255),
    finalized     BOOLEAN                     NOT NULL,
    stock_id      UUID                        NOT NULL,
    planting_id   UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_harvests PRIMARY KEY (id)
);

ALTER TABLE harvests
    ADD CONSTRAINT uc_harvests_planting UNIQUE (planting_id);

CREATE INDEX idx_harvest_date ON harvests (harvest_date);

ALTER TABLE harvests
    ADD CONSTRAINT FK_HARVESTS_ON_PLANTING FOREIGN KEY (planting_id) REFERENCES plantings (id);

CREATE INDEX idx_harvest_planting ON harvests (planting_id);

ALTER TABLE harvests
    ADD CONSTRAINT FK_HARVESTS_ON_STOCK FOREIGN KEY (stock_id) REFERENCES stocks (id);

CREATE TABLE harvest_forecasts
(
    id                 UUID                        NOT NULL,
    crop_id            UUID                        NOT NULL,
    season_id          UUID                        NOT NULL,
    property_id        UUID                        NOT NULL,
    planting_id        UUID                        NOT NULL,
    estimated_quantity DECIMAL(10, 2)              NOT NULL,
    forecast_date      date                        NOT NULL,
    confidence_level   VARCHAR(255)                NOT NULL,
    planted_area       DECIMAL(5, 2),
    notes              TEXT,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_harvest_forecasts PRIMARY KEY (id)
);

ALTER TABLE harvest_forecasts
    ADD CONSTRAINT FK_HARVEST_FORECASTS_ON_CROP FOREIGN KEY (crop_id) REFERENCES crops (id);

ALTER TABLE harvest_forecasts
    ADD CONSTRAINT FK_HARVEST_FORECASTS_ON_PLANTING FOREIGN KEY (planting_id) REFERENCES plantings (id);

ALTER TABLE harvest_forecasts
    ADD CONSTRAINT FK_HARVEST_FORECASTS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

ALTER TABLE harvest_forecasts
    ADD CONSTRAINT FK_HARVEST_FORECASTS_ON_SEASON FOREIGN KEY (season_id) REFERENCES seasons (id);

CREATE TABLE harvest_partials
(
    id                  UUID                        NOT NULL,
    harvest_id          UUID                        NOT NULL,
    partial_date        date                        NOT NULL,
    quantity_kg         DECIMAL(12, 3)              NOT NULL,
    quality_grade       VARCHAR(50),
    notes               TEXT,
    responsible_user_id UUID                        NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_harvest_partials PRIMARY KEY (id)
);

CREATE INDEX idx_partial_date ON harvest_partials (partial_date);

ALTER TABLE harvest_partials
    ADD CONSTRAINT FK_HARVEST_PARTIALS_ON_HARVEST FOREIGN KEY (harvest_id) REFERENCES harvests (id);

CREATE INDEX idx_partial_harvest ON harvest_partials (harvest_id);

ALTER TABLE harvest_partials
    ADD CONSTRAINT FK_HARVEST_PARTIALS_ON_RESPONSIBLE_USER FOREIGN KEY (responsible_user_id) REFERENCES users (id);

CREATE TABLE plantings
(
    id                    UUID                        NOT NULL,
    area_hectares         DECIMAL(12, 2)              NOT NULL,
    planting_date         date                        NOT NULL,
    expected_harvest_date date                        NOT NULL,
    planting_status       VARCHAR(255)                NOT NULL,
    property_id           UUID                        NOT NULL,
    field_id              UUID                        NOT NULL,
    crop_id               UUID                        NOT NULL,
    crop_variety_id       UUID                        NOT NULL,
    season_id             UUID                        NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_plantings PRIMARY KEY (id)
);

CREATE INDEX idx_planting_status ON plantings (planting_status);

ALTER TABLE plantings
    ADD CONSTRAINT FK_PLANTINGS_ON_CROP FOREIGN KEY (crop_id) REFERENCES crops (id);

CREATE INDEX idx_planting_crop ON plantings (crop_id);

ALTER TABLE plantings
    ADD CONSTRAINT FK_PLANTINGS_ON_CROP_VARIETY FOREIGN KEY (crop_variety_id) REFERENCES crop_varieties (id);

ALTER TABLE plantings
    ADD CONSTRAINT FK_PLANTINGS_ON_FIELD FOREIGN KEY (field_id) REFERENCES fields (id);

CREATE INDEX idx_planting_field ON plantings (field_id);

ALTER TABLE plantings
    ADD CONSTRAINT FK_PLANTINGS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

CREATE INDEX idx_planting_property ON plantings (property_id);

ALTER TABLE plantings
    ADD CONSTRAINT FK_PLANTINGS_ON_SEASON FOREIGN KEY (season_id) REFERENCES seasons (id);

CREATE INDEX idx_planting_season ON plantings (season_id);

CREATE TABLE planting_inputs
(
    id               UUID                        NOT NULL,
    planting_id      UUID                        NOT NULL,
    input_id         UUID                        NOT NULL,
    quantity         DECIMAL(12, 3)              NOT NULL,
    unit             VARCHAR(30),
    application_date date,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_planting_inputs PRIMARY KEY (id)
);

ALTER TABLE planting_inputs
    ADD CONSTRAINT FK_PLANTING_INPUTS_ON_INPUT FOREIGN KEY (input_id) REFERENCES inputs (id);

ALTER TABLE planting_inputs
    ADD CONSTRAINT FK_PLANTING_INPUTS_ON_PLANTING FOREIGN KEY (planting_id) REFERENCES plantings (id);

CREATE TABLE seasons
(
    id            UUID                        NOT NULL,
    name          VARCHAR(255)                NOT NULL,
    start_date    date                        NOT NULL,
    end_date      date                        NOT NULL,
    season_status VARCHAR(255)                NOT NULL,
    property_id   UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_seasons PRIMARY KEY (id)
);

ALTER TABLE seasons
    ADD CONSTRAINT uc_e04b6948dac0ec71908cb603a UNIQUE (property_id, name);

CREATE INDEX idx_season_dates ON seasons (start_date, end_date);

CREATE INDEX idx_season_status ON seasons (season_status);

ALTER TABLE seasons
    ADD CONSTRAINT FK_SEASONS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

CREATE TABLE yields
(
    id                       UUID                        NOT NULL,
    harvest_id               UUID                        NOT NULL,
    field_id                 UUID                        NOT NULL,
    total_produced_kg        DECIMAL(10, 2)              NOT NULL,
    productivity_per_hectare DECIMAL(10, 2)              NOT NULL,
    losses_kg                DECIMAL(10, 2),
    notes                    TEXT,
    created_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    moisture_percentage      DECIMAL,
    impurity_percentage      DECIMAL,
    quality_grade            VARCHAR(255),
    CONSTRAINT pk_yields PRIMARY KEY (id)
);

ALTER TABLE yields
    ADD CONSTRAINT FK_YIELDS_ON_FIELD FOREIGN KEY (field_id) REFERENCES fields (id);

CREATE INDEX idx_yield_field ON yields (field_id);

ALTER TABLE yields
    ADD CONSTRAINT FK_YIELDS_ON_HARVEST FOREIGN KEY (harvest_id) REFERENCES harvests (id);

CREATE INDEX idx_yield_harvest ON yields (harvest_id);

CREATE TABLE agronomic_prescriptions
(
    id              UUID         NOT NULL,
    field_id        UUID         NOT NULL,
    planting_id     UUID         NOT NULL,
    agronomist_name VARCHAR(255) NOT NULL,
    agronomist_crea VARCHAR(20)  NOT NULL,
    issued_at       date         NOT NULL,
    valid_until     date         NOT NULL,
    document_url    VARCHAR(500) NOT NULL,
    active          BOOLEAN      NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_agronomic_prescriptions PRIMARY KEY (id)
);

ALTER TABLE agronomic_prescriptions
    ADD CONSTRAINT FK_AGRONOMIC_PRESCRIPTIONS_ON_FIELD FOREIGN KEY (field_id) REFERENCES fields (id);

ALTER TABLE agronomic_prescriptions
    ADD CONSTRAINT FK_AGRONOMIC_PRESCRIPTIONS_ON_PLANTING FOREIGN KEY (planting_id) REFERENCES plantings (id);

CREATE TABLE prescription_items
(
    id                  UUID           NOT NULL,
    prescription_id     UUID           NOT NULL,
    input_id            UUID           NOT NULL,
    authorized_quantity DECIMAL(10, 3) NOT NULL,
    unit                VARCHAR(20)    NOT NULL,
    usage_instructions  TEXT,
    CONSTRAINT pk_prescription_items PRIMARY KEY (id)
);

ALTER TABLE prescription_items
    ADD CONSTRAINT uc_090df3d240c0e950f34626b91 UNIQUE (prescription_id, input_id);

ALTER TABLE prescription_items
    ADD CONSTRAINT FK_PRESCRIPTION_ITEMS_ON_INPUT FOREIGN KEY (input_id) REFERENCES inputs (id);

ALTER TABLE prescription_items
    ADD CONSTRAINT FK_PRESCRIPTION_ITEMS_ON_PRESCRIPTION FOREIGN KEY (prescription_id) REFERENCES agronomic_prescriptions (id);