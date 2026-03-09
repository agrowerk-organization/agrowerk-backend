CREATE TABLE barter_contracts
(
    id                   UUID                        NOT NULL,
    transaction_id       UUID                        NOT NULL,
    contract_number      VARCHAR(50)                 NOT NULL,
    start_date           date                        NOT NULL,
    end_date             date                        NOT NULL,
    contract_status      VARCHAR(255)                NOT NULL,
    terms_and_conditions TEXT,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_barter_contracts PRIMARY KEY (id)
);

CREATE TABLE contract_documents
(
    contract_id      UUID NOT NULL,
    file_metadata_id UUID NOT NULL
);

ALTER TABLE barter_contracts
    ADD CONSTRAINT uc_barter_contracts_contractnumber UNIQUE (contract_number);

ALTER TABLE barter_contracts
    ADD CONSTRAINT uc_barter_contracts_transaction UNIQUE (transaction_id);

ALTER TABLE barter_contracts
    ADD CONSTRAINT FK_BARTER_CONTRACTS_ON_TRANSACTION FOREIGN KEY (transaction_id) REFERENCES barter_transactions (id);

ALTER TABLE contract_documents
    ADD CONSTRAINT fk_condoc_on_barter_contract FOREIGN KEY (contract_id) REFERENCES barter_contracts (id);

ALTER TABLE contract_documents
    ADD CONSTRAINT fk_condoc_on_file_metadata FOREIGN KEY (file_metadata_id) REFERENCES file_metadata (id);

CREATE TABLE barter_offers
(
    id                     UUID                        NOT NULL,
    title                  VARCHAR(255)                NOT NULL,
    description            TEXT,
    owner_id               UUID                        NOT NULL,
    property_id            UUID,
    offer_type             VARCHAR(255)                NOT NULL,
    offered_crop_id        UUID,
    offered_crop_quantity  DECIMAL(10, 2),
    estimated_harvest_date date,
    offered_asset_id       UUID,
    offered_asset_quantity DECIMAL(10, 2),
    requested_type         VARCHAR(255)                NOT NULL,
    requested_description  TEXT,
    requested_value        DECIMAL(10, 2),
    status                 VARCHAR(255)                NOT NULL,
    region                 VARCHAR(255),
    expires_at             date,
    view_count             INTEGER                     NOT NULL,
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_barter_offers PRIMARY KEY (id)
);

ALTER TABLE barter_offers
    ADD CONSTRAINT FK_BARTER_OFFERS_ON_OFFERED_ASSET FOREIGN KEY (offered_asset_id) REFERENCES invetory_assets (id);

ALTER TABLE barter_offers
    ADD CONSTRAINT FK_BARTER_OFFERS_ON_OFFERED_CROP FOREIGN KEY (offered_crop_id) REFERENCES crops (id);

ALTER TABLE barter_offers
    ADD CONSTRAINT FK_BARTER_OFFERS_ON_OWNER FOREIGN KEY (owner_id) REFERENCES users (id);

ALTER TABLE barter_offers
    ADD CONSTRAINT FK_BARTER_OFFERS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

CREATE TABLE barter_transactions
(
    id                      UUID                        NOT NULL,
    offer_id                UUID                        NOT NULL,
    offeror_id              UUID                        NOT NULL,
    acceptor_id             UUID                        NOT NULL,
    offeror_gives           VARCHAR(255)                NOT NULL,
    offeror_crop_id         UUID,
    offeror_crop_quantity   DECIMAL(10, 2),
    offeror_asset_id        UUID,
    offeror_asset_quantity  DECIMAL(10, 2),
    acceptor_gives          VARCHAR(255)                NOT NULL,
    acceptor_crop_id        UUID,
    acceptor_crop_quantity  DECIMAL(10, 2),
    acceptor_asset_id       UUID,
    acceptor_asset_quantity DECIMAL(10, 2),
    status                  VARCHAR(255)                NOT NULL,
    offeror_delivery_date   date,
    acceptor_delivery_date  date,
    notes                   TEXT,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_barter_transactions PRIMARY KEY (id)
);

ALTER TABLE barter_transactions
    ADD CONSTRAINT FK_BARTER_TRANSACTIONS_ON_ACCEPTOR FOREIGN KEY (acceptor_id) REFERENCES users (id);

ALTER TABLE barter_transactions
    ADD CONSTRAINT FK_BARTER_TRANSACTIONS_ON_ACCEPTOR_ASSET FOREIGN KEY (acceptor_asset_id) REFERENCES invetory_assets (id);

ALTER TABLE barter_transactions
    ADD CONSTRAINT FK_BARTER_TRANSACTIONS_ON_ACCEPTOR_CROP FOREIGN KEY (acceptor_crop_id) REFERENCES crops (id);

ALTER TABLE barter_transactions
    ADD CONSTRAINT FK_BARTER_TRANSACTIONS_ON_OFFER FOREIGN KEY (offer_id) REFERENCES barter_offers (id);

ALTER TABLE barter_transactions
    ADD CONSTRAINT FK_BARTER_TRANSACTIONS_ON_OFFEROR FOREIGN KEY (offeror_id) REFERENCES users (id);

ALTER TABLE barter_transactions
    ADD CONSTRAINT FK_BARTER_TRANSACTIONS_ON_OFFEROR_ASSET FOREIGN KEY (offeror_asset_id) REFERENCES invetory_assets (id);

ALTER TABLE barter_transactions
    ADD CONSTRAINT FK_BARTER_TRANSACTIONS_ON_OFFEROR_CROP FOREIGN KEY (offeror_crop_id) REFERENCES crops (id);

CREATE TABLE crop_commitments
(
    id                     UUID                        NOT NULL,
    transaction_id         UUID                        NOT NULL,
    farmer_id              UUID                        NOT NULL,
    crop_id                UUID                        NOT NULL,
    committed_quantity     DECIMAL(10, 2)              NOT NULL,
    delivered_quantity     DECIMAL(10, 2),
    expected_delivery_date date                        NOT NULL,
    actual_delivery_date   date,
    status                 VARCHAR(255)                NOT NULL,
    notes                  TEXT,
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_crop_commitments PRIMARY KEY (id)
);

ALTER TABLE crop_commitments
    ADD CONSTRAINT FK_CROP_COMMITMENTS_ON_CROP FOREIGN KEY (crop_id) REFERENCES crops (id);

ALTER TABLE crop_commitments
    ADD CONSTRAINT FK_CROP_COMMITMENTS_ON_FARMER FOREIGN KEY (farmer_id) REFERENCES users (id);

ALTER TABLE crop_commitments
    ADD CONSTRAINT FK_CROP_COMMITMENTS_ON_TRANSACTION FOREIGN KEY (transaction_id) REFERENCES barter_transactions (id);

CREATE TABLE delivery_schedules
(
    id                   UUID                        NOT NULL,
    transaction_id       UUID                        NOT NULL,
    commitment_id        UUID,
    scheduled_date       date                        NOT NULL,
    scheduled_quantity   DECIMAL(10, 2),
    delivered_quantity   DECIMAL(10, 2),
    actual_delivery_date date,
    status               VARCHAR(255)                NOT NULL,
    delivery_address     TEXT,
    notes                TEXT,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_delivery_schedules PRIMARY KEY (id)
);

ALTER TABLE delivery_schedules
    ADD CONSTRAINT FK_DELIVERY_SCHEDULES_ON_COMMITMENT FOREIGN KEY (commitment_id) REFERENCES crop_commitments (id);

ALTER TABLE delivery_schedules
    ADD CONSTRAINT FK_DELIVERY_SCHEDULES_ON_TRANSACTION FOREIGN KEY (transaction_id) REFERENCES barter_transactions (id);

CREATE TABLE partial_deliveries
(
    id                  UUID                        NOT NULL,
    commitment_id       UUID                        NOT NULL,
    delivered_quantity  DECIMAL(10, 2)              NOT NULL,
    delivery_date       date                        NOT NULL,
    moisture_percentage DECIMAL(5, 2),
    impurity_percentage DECIMAL(5, 2),
    quality_grade       VARCHAR(50),
    notes               TEXT,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_partial_deliveries PRIMARY KEY (id)
);

CREATE TABLE partial_delivery_documents
(
    delivery_id      UUID NOT NULL,
    file_metadata_id UUID NOT NULL
);

ALTER TABLE partial_deliveries
    ADD CONSTRAINT FK_PARTIAL_DELIVERIES_ON_COMMITMENT FOREIGN KEY (commitment_id) REFERENCES crop_commitments (id);

ALTER TABLE partial_delivery_documents
    ADD CONSTRAINT fk_pardeldoc_on_file_metadata FOREIGN KEY (file_metadata_id) REFERENCES file_metadata (id);

ALTER TABLE partial_delivery_documents
    ADD CONSTRAINT fk_pardeldoc_on_partial_delivery FOREIGN KEY (delivery_id) REFERENCES partial_deliveries (id);

