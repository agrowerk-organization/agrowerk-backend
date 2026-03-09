CREATE TABLE properties
(
    id                 UUID                        NOT NULL,
    name               VARCHAR(100)                NOT NULL,
    state_registration VARCHAR(18),
    rural_registration VARCHAR(20),
    latitude           DECIMAL(9, 6),
    longitude          DECIMAL(9, 6),
    total_area         DECIMAL(10, 2),
    planted_area       DECIMAL(10, 2),
    main_crop          VARCHAR(255),
    is_active          BOOLEAN                     NOT NULL,
    state_id           UUID,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    municipality       VARCHAR(255),
    code               VARCHAR(9),
    address_number     INTEGER,
    street             VARCHAR(255),
    neighborhood       VARCHAR(255),
    CONSTRAINT pk_properties PRIMARY KEY (id)
);

ALTER TABLE properties
    ADD CONSTRAINT uc_properties_stateregistration UNIQUE (state_registration);

ALTER TABLE properties
    ADD CONSTRAINT FK_PROPERTIES_ON_STATE FOREIGN KEY (state_id) REFERENCES states (id);

CREATE TABLE states
(
    id         UUID                        NOT NULL,
    code       VARCHAR(2)                  NOT NULL,
    name       VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_states PRIMARY KEY (id)
);

ALTER TABLE states
    ADD CONSTRAINT uc_states_code UNIQUE (code);

CREATE TABLE user_property
(
    id                  UUID                        NOT NULL,
    user_id             UUID                        NOT NULL,
    property_id         UUID                        NOT NULL,
    is_master_owner     BOOLEAN                     NOT NULL,
    joined_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    can_edit            BOOLEAN                     NOT NULL,
    can_edit_granted_at TIMESTAMP WITHOUT TIME ZONE,
    can_edit_granted_by UUID,
    removed_at          TIMESTAMP WITHOUT TIME ZONE,
    removed_by          UUID,
    removal_reason      VARCHAR(255),
    is_active           BOOLEAN                     NOT NULL,
    CONSTRAINT pk_user_property PRIMARY KEY (id)
);

ALTER TABLE user_property
    ADD CONSTRAINT FK_USER_PROPERTY_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

ALTER TABLE user_property
    ADD CONSTRAINT FK_USER_PROPERTY_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);