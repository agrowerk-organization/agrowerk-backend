CREATE TABLE inputs
(
    id                     UUID                        NOT NULL,
    name                   VARCHAR(150)                NOT NULL,
    internal_code          VARCHAR(50),
    manufacturer_code      VARCHAR(50),
    description            TEXT,
    unit_of_measure        VARCHAR(20)                 NOT NULL,
    active_ingredient      VARCHAR(100),
    formulation            VARCHAR(100),
    concentration          VARCHAR(50),
    mapa_registration      VARCHAR(50),
    toxicological_class    VARCHAR(20),
    grace_period           INTEGER,
    minimum_stock          DECIMAL(10, 2),
    maximum_stock          DECIMAL(10, 2),
    average_purchase_price DECIMAL(10, 2),
    last_purchase_price    DECIMAL(10, 2),
    active                 BOOLEAN                     NOT NULL,
    controlled             BOOLEAN                     NOT NULL,
    category_id            UUID                        NOT NULL,
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_inputs PRIMARY KEY (id)
);

ALTER TABLE inputs
    ADD CONSTRAINT uc_inputs_internalcode UNIQUE (internal_code);

ALTER TABLE inputs
    ADD CONSTRAINT FK_INPUTS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES input_categories (id);

CREATE TABLE input_categories
(
    id               UUID                        NOT NULL,
    name             VARCHAR(100)                NOT NULL,
    description      TEXT,
    unit_of_measure  VARCHAR(20)                 NOT NULL,
    icon             VARCHAR(50),
    color            VARCHAR(7),
    is_active        BOOLEAN                     NOT NULL,
    requires_license BOOLEAN                     NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_input_categories PRIMARY KEY (id)
);

ALTER TABLE input_categories
    ADD CONSTRAINT uc_input_categories_name UNIQUE (name);

CREATE TABLE input_crops
(
    id                           UUID                        NOT NULL,
    input_id                     UUID                        NOT NULL,
    crop_id                      UUID                        NOT NULL,
    usage_recommendation         TEXT,
    recommended_dose_per_hectare DECIMAL(10, 3),
    dose_unit                    VARCHAR(20),
    approved_by_admin            BOOLEAN                     NOT NULL,
    approved_by                  UUID,
    approved_at                  TIMESTAMP WITHOUT TIME ZONE,
    created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_input_crops PRIMARY KEY (id)
);

ALTER TABLE input_crops
    ADD CONSTRAINT uc_72bfea3e1df39b9f16b863c47 UNIQUE (input_id, crop_id);

ALTER TABLE input_crops
    ADD CONSTRAINT FK_INPUT_CROPS_ON_APPROVED_BY FOREIGN KEY (approved_by) REFERENCES users (id);

ALTER TABLE input_crops
    ADD CONSTRAINT FK_INPUT_CROPS_ON_CROP FOREIGN KEY (crop_id) REFERENCES crops (id);

CREATE INDEX idx_input_crop_crop ON input_crops (crop_id);

ALTER TABLE input_crops
    ADD CONSTRAINT FK_INPUT_CROPS_ON_INPUT FOREIGN KEY (input_id) REFERENCES inputs (id);

CREATE INDEX idx_input_crop_input ON input_crops (input_id);

CREATE TABLE invetory_assets
(
    id                            UUID                        NOT NULL,
    name                          VARCHAR(255)                NOT NULL,
    description                   TEXT,
    category                      VARCHAR(255)                NOT NULL,
    condition                     VARCHAR(255)                NOT NULL,
    quantity                      INTEGER                     NOT NULL,
    reference_value               DECIMAL(10, 2)              NOT NULL,
    unit                          VARCHAR(255),
    valuation_method              VARCHAR(255)                NOT NULL,
    agreed_value                  DECIMAL(10, 2),
    commodity_reference           VARCHAR(50),
    commodity_quantity_equivalent DECIMAL(10, 3),
    user_id                       UUID                        NOT NULL,
    property_id                   UUID,
    available                     BOOLEAN                     NOT NULL,
    created_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_invetory_assets PRIMARY KEY (id)
);

ALTER TABLE invetory_assets
    ADD CONSTRAINT FK_INVETORY_ASSETS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

ALTER TABLE invetory_assets
    ADD CONSTRAINT FK_INVETORY_ASSETS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE stocks
(
    id                    UUID                        NOT NULL,
    property_id           UUID                        NOT NULL,
    input_id              UUID,
    stock_type            VARCHAR(255)                NOT NULL,
    current_quantity      DECIMAL(10, 3)              NOT NULL,
    reserved_quantity     DECIMAL(10, 3),
    total_value           DECIMAL(12, 2),
    weighted_average_cost DECIMAL(10, 2),
    last_entry_date       TIMESTAMP WITHOUT TIME ZONE,
    last_exit_date        TIMESTAMP WITHOUT TIME ZONE,
    warehouse_id          UUID,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_stocks PRIMARY KEY (id)
);

ALTER TABLE stocks
    ADD CONSTRAINT uc_5cff4ab050dd47eed13ed7ccd UNIQUE (property_id, input_id, stock_type);

ALTER TABLE stocks
    ADD CONSTRAINT FK_STOCKS_ON_INPUT FOREIGN KEY (input_id) REFERENCES inputs (id);

ALTER TABLE stocks
    ADD CONSTRAINT FK_STOCKS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

ALTER TABLE stocks
    ADD CONSTRAINT FK_STOCKS_ON_WAREHOUSE FOREIGN KEY (warehouse_id) REFERENCES warehouses (id);

CREATE TABLE movement_stocks
(
    id                   UUID                        NOT NULL,
    quantity             DECIMAL(10, 3)              NOT NULL,
    unit_value           DECIMAL(10, 2),
    total_value          DECIMAL(12, 2),
    destination          VARCHAR(255),
    crop                 VARCHAR(255),
    notes                TEXT,
    justification        TEXT,
    document_number      VARCHAR(50),
    movement_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    reversed_movement_id UUID,
    reversed             BOOLEAN                     NOT NULL,
    movement_type        VARCHAR(30)                 NOT NULL,
    user_id              UUID                        NOT NULL,
    stock_id             UUID                        NOT NULL,
    property_id          UUID                        NOT NULL,
    batch_id             UUID,
    harvest_id           UUID,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_movement_stocks PRIMARY KEY (id)
);

ALTER TABLE movement_stocks
    ADD CONSTRAINT uc_movement_stocks_harvest UNIQUE (harvest_id);

ALTER TABLE movement_stocks
    ADD CONSTRAINT FK_MOVEMENT_STOCKS_ON_BATCH FOREIGN KEY (batch_id) REFERENCES batchs (id);

ALTER TABLE movement_stocks
    ADD CONSTRAINT FK_MOVEMENT_STOCKS_ON_HARVEST FOREIGN KEY (harvest_id) REFERENCES harvests (id);

ALTER TABLE movement_stocks
    ADD CONSTRAINT FK_MOVEMENT_STOCKS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

ALTER TABLE movement_stocks
    ADD CONSTRAINT FK_MOVEMENT_STOCKS_ON_STOCK FOREIGN KEY (stock_id) REFERENCES stocks (id);

ALTER TABLE movement_stocks
    ADD CONSTRAINT FK_MOVEMENT_STOCKS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE warehouses
(
    id                   UUID                        NOT NULL,
    name                 VARCHAR(100)                NOT NULL,
    code                 VARCHAR(20),
    warehouse_type       VARCHAR(255)                NOT NULL,
    capacity_kg          DECIMAL(12, 2),
    current_occupancy_kg DECIMAL(12, 2),
    location             VARCHAR(200),
    description          TEXT,
    is_active            BOOLEAN                     NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    property_id          UUID                        NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_warehouses PRIMARY KEY (id)
);

ALTER TABLE warehouses
    ADD CONSTRAINT FK_WAREHOUSES_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);