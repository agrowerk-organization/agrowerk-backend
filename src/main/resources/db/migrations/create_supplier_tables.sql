CREATE TABLE supplier_crops
(
    crop_id     UUID NOT NULL,
    supplier_id UUID NOT NULL,
    CONSTRAINT pk_supplier_crops PRIMARY KEY (crop_id, supplier_id)
);

CREATE TABLE suppliers
(
    id                   UUID                        NOT NULL,
    corporate_reason     VARCHAR(255)                NOT NULL,
    fantasy_name         VARCHAR(255),
    cnpj                 VARCHAR(18)                 NOT NULL,
    state_registration   VARCHAR(255),
    email                VARCHAR(255)                NOT NULL,
    telephone            VARCHAR(15),
    name_contact         VARCHAR(255),
    observations         TEXT,
    is_active            BOOLEAN                     NOT NULL,
    accepts_barter_deals BOOLEAN                     NOT NULL,
    barter_terms         TEXT,
    administrator_id     UUID                        NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    municipality         VARCHAR(255),
    code                 VARCHAR(9),
    address_number       INTEGER,
    street               VARCHAR(255),
    neighborhood         VARCHAR(255),
    CONSTRAINT pk_suppliers PRIMARY KEY (id)
);

ALTER TABLE suppliers
    ADD CONSTRAINT uc_suppliers_administrator UNIQUE (administrator_id);

ALTER TABLE suppliers
    ADD CONSTRAINT uc_suppliers_cnpj UNIQUE (cnpj);

ALTER TABLE suppliers
    ADD CONSTRAINT FK_SUPPLIERS_ON_ADMINISTRATOR FOREIGN KEY (administrator_id) REFERENCES users (id);

ALTER TABLE supplier_crops
    ADD CONSTRAINT fk_supcro_on_crop FOREIGN KEY (crop_id) REFERENCES crops (id);

ALTER TABLE supplier_crops
    ADD CONSTRAINT fk_supcro_on_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id);

CREATE TABLE supplier_ratings
(
    id          UUID                        NOT NULL,
    supplier_id UUID                        NOT NULL,
    rated_by_id UUID                        NOT NULL,
    rating      DECIMAL(3, 2)               NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_supplier_ratings PRIMARY KEY (id)
);

ALTER TABLE supplier_ratings
    ADD CONSTRAINT uc_9097cbed4203c38828bb44e40 UNIQUE (supplier_id, rated_by_id);

ALTER TABLE supplier_ratings
    ADD CONSTRAINT FK_SUPPLIER_RATINGS_ON_RATED_BY FOREIGN KEY (rated_by_id) REFERENCES users (id);

CREATE INDEX idx_rating_user ON supplier_ratings (rated_by_id);

ALTER TABLE supplier_ratings
    ADD CONSTRAINT FK_SUPPLIER_RATINGS_ON_SUPPLIER FOREIGN KEY (supplier_id) REFERENCES suppliers (id);

CREATE INDEX idx_rating_supplier ON supplier_ratings (supplier_id);

CREATE TABLE supplier_specialties
(
    id          UUID                        NOT NULL,
    name        VARCHAR(255),
    description TEXT,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_supplier_specialties PRIMARY KEY (id)
);

CREATE TABLE supplier_specialty_link
(
    id           UUID                        NOT NULL,
    supplier_id  UUID                        NOT NULL,
    specialty_id UUID                        NOT NULL,
    is_active    BOOLEAN,
    since        TIMESTAMP WITHOUT TIME ZONE,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_supplier_specialty_link PRIMARY KEY (id)
);

ALTER TABLE supplier_specialty_link
    ADD CONSTRAINT FK_SUPPLIER_SPECIALTY_LINK_ON_SPECIALTY FOREIGN KEY (specialty_id) REFERENCES supplier_specialties (id);

ALTER TABLE supplier_specialty_link
    ADD CONSTRAINT FK_SUPPLIER_SPECIALTY_LINK_ON_SUPPLIER FOREIGN KEY (supplier_id) REFERENCES suppliers (id);