CREATE TABLE barter_offers (
                               id UUID PRIMARY KEY,
                               title VARCHAR(255) NOT NULL,
                               description TEXT,
                               owner_id UUID NOT NULL,
                               property_id UUID,
                               offer_type VARCHAR(50) NOT NULL,
                               offered_forecast_id UUID,
                               offered_crop_quantity DECIMAL(10, 2),
                               estimated_harvest_date DATE,
                               offered_asset_id UUID,
                               offered_asset_quantity DECIMAL(10, 2),
                               requested_type VARCHAR(50) NOT NULL,
                               requested_description TEXT,
                               requested_value DECIMAL(10, 2),
                               status VARCHAR(50) NOT NULL,
                               expires_at DATE,
                               view_count INTEGER NOT NULL DEFAULT 0,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL
);

CREATE TABLE barter_transactions (
                                     id UUID PRIMARY KEY,
                                     barter_offer_id UUID NOT NULL,
                                     offeror_id UUID NOT NULL,
                                     acceptor_id UUID NOT NULL,
                                     offeror_gives VARCHAR(50) NOT NULL,
                                     offeror_crop_id UUID,
                                     offeror_crop_quantity DECIMAL(10, 2),
                                     offeror_asset_id UUID,
                                     offeror_asset_quantity DECIMAL(10, 2),
                                     acceptor_gives VARCHAR(50) NOT NULL,
                                     acceptor_crop_id UUID,
                                     acceptor_crop_quantity DECIMAL(10, 2),
                                     acceptor_asset_id UUID,
                                     acceptor_asset_quantity DECIMAL(10, 2),
                                     status VARCHAR(50) NOT NULL,
                                     offeror_delivery_date DATE,
                                     acceptor_delivery_date DATE,
                                     notes TEXT,
                                     created_at TIMESTAMP NOT NULL,
                                     updated_at TIMESTAMP NOT NULL,
                                     CONSTRAINT fk_bt_offer FOREIGN KEY (barter_offer_id) REFERENCES barter_offers(id)
);

ALTER TABLE barter_transactions
    ADD COLUMN offeror_batch_id UUID REFERENCES batchs(id);

CREATE TABLE barter_offer_items (
                                    id UUID PRIMARY KEY,
                                    offer_id UUID NOT NULL,
                                    input_id UUID NOT NULL,
                                    quantity DECIMAL(10, 2) NOT NULL,
                                    unit_of_measure VARCHAR(50) NOT NULL,
                                    unit_price_brl DECIMAL(10, 2) NOT NULL,
                                    total_price_brl DECIMAL(10, 2) NOT NULL,
                                    notes TEXT,
                                    created_at TIMESTAMP NOT NULL,
                                    CONSTRAINT fk_boi_offer FOREIGN KEY (offer_id) REFERENCES barter_offers(id)
);

CREATE TABLE barter_transaction_items (
                                          id UUID PRIMARY KEY,
                                          transaction_id UUID NOT NULL,
                                          input_id UUID NOT NULL,
                                          quantity DECIMAL(10, 2) NOT NULL,
                                          unit_of_measure VARCHAR(50) NOT NULL,
                                          unit_price_brl DECIMAL(10, 2) NOT NULL,
                                          total_price_brl DECIMAL(10, 2) NOT NULL,
                                          created_at TIMESTAMP NOT NULL,
                                          CONSTRAINT fk_bti_transaction FOREIGN KEY (transaction_id) REFERENCES barter_transactions(id)
);


CREATE TABLE barter_contracts (
                                  id UUID PRIMARY KEY,
                                  transaction_id UUID NOT NULL UNIQUE,
                                  contract_number VARCHAR(50) NOT NULL UNIQUE,
                                  start_date DATE NOT NULL,
                                  end_date DATE NOT NULL,
                                  contract_status VARCHAR(50) NOT NULL,
                                  terms_and_conditions TEXT,
                                  offeror_signed_at TIMESTAMP,
                                  offeror_sign_ip VARCHAR(45),
                                  acceptor_signed_at TIMESTAMP,
                                  acceptor_sign_ip VARCHAR(45),
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP NOT NULL,
                                  CONSTRAINT fk_bc_transaction FOREIGN KEY (transaction_id) REFERENCES barter_transactions(id)
);

CREATE TABLE barter_price_snapshots (
                                        id UUID PRIMARY KEY,
                                        transaction_id UUID NOT NULL UNIQUE,
                                        commodity VARCHAR(20) NOT NULL,
                                        cbot_contract_month VARCHAR(10) NOT NULL,
                                        cbot_price_usd DECIMAL(10, 4) NOT NULL,
                                        ptax_rate DECIMAL(10, 4) NOT NULL,
                                        ptax_reference_date DATE NOT NULL,
                                        basis_usd DECIMAL(10, 4) NOT NULL,
                                        bag_price_brl DECIMAL(10, 2) NOT NULL,
                                        total_value_brl DECIMAL(12, 2) NOT NULL,
                                        total_bags_due DECIMAL(10, 4) NOT NULL,
                                        snapshot_at TIMESTAMP NOT NULL,
                                        created_at TIMESTAMP NOT NULL,
                                        CONSTRAINT fk_bps_transaction FOREIGN KEY (transaction_id) REFERENCES barter_transactions(id)
);


CREATE TABLE crop_commitments (
                                  id UUID PRIMARY KEY,
                                  transaction_id UUID NOT NULL,
                                  farmer_id UUID NOT NULL,
                                  crop_id UUID NOT NULL,
                                  committed_quantity DECIMAL(10, 2) NOT NULL,
                                  delivered_quantity DECIMAL(10, 2) DEFAULT 0.00,
                                  expected_delivery_date DATE NOT NULL,
                                  actual_delivery_date DATE,
                                  status VARCHAR(50) NOT NULL,
                                  notes TEXT,
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP NOT NULL,
                                  CONSTRAINT fk_cc_transaction FOREIGN KEY (transaction_id) REFERENCES barter_transactions(id)
);

CREATE TABLE delivery_schedules (
                                    id UUID PRIMARY KEY,
                                    transaction_id UUID NOT NULL,
                                    commitment_id UUID,
                                    scheduled_date DATE NOT NULL,
                                    scheduled_quantity DECIMAL(10, 2),
                                    delivered_quantity DECIMAL(10, 2),
                                    actual_delivery_date DATE,
                                    status VARCHAR(50) NOT NULL,
                                    delivery_address TEXT,
                                    notes TEXT,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP NOT NULL,
                                    CONSTRAINT fk_ds_transaction FOREIGN KEY (transaction_id) REFERENCES barter_transactions(id),
                                    CONSTRAINT fk_ds_commitment FOREIGN KEY (commitment_id) REFERENCES crop_commitments(id)
);

CREATE TABLE partial_deliveries (
                                    id UUID PRIMARY KEY,
                                    commitment_id UUID NOT NULL,
                                    delivered_quantity DECIMAL(10, 2) NOT NULL,
                                    delivery_date DATE NOT NULL,
                                    moisture_percentage DECIMAL(5, 2),
                                    impurity_percentage DECIMAL(5, 2),
                                    quality_grade VARCHAR(50),
                                    notes TEXT,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP NOT NULL,
                                    CONSTRAINT fk_pd_commitment FOREIGN KEY (commitment_id) REFERENCES crop_commitments(id)
);


CREATE TABLE partial_delivery_documents (
                                            delivery_id UUID NOT NULL,
                                            file_metadata_id UUID NOT NULL,
                                            PRIMARY KEY (delivery_id, file_metadata_id),
                                            CONSTRAINT fk_pdd_delivery FOREIGN KEY (delivery_id) REFERENCES partial_deliveries(id)
);

CREATE TABLE contract_documents (
                                    contract_id UUID NOT NULL,
                                    file_metadata_id UUID NOT NULL,
                                    PRIMARY KEY (contract_id, file_metadata_id),
                                    CONSTRAINT fk_cd_contract FOREIGN KEY (contract_id) REFERENCES barter_contracts(id)
);


CREATE INDEX idx_bo_status ON barter_offers(status);
CREATE INDEX idx_bo_owner_id ON barter_offers(owner_id);
CREATE INDEX idx_bt_status ON barter_transactions(status);
CREATE INDEX idx_cc_expected_delivery ON crop_commitments(expected_delivery_date);
CREATE INDEX idx_bps_commodity ON barter_price_snapshots(commodity);




