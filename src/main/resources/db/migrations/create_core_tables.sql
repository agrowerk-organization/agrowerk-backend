CREATE TABLE roles
(
    id         UUID                        NOT NULL,
    name       VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);


CREATE TABLE users
(
    id                         UUID                        NOT NULL,
    name                       VARCHAR(255)                NOT NULL,
    email                      VARCHAR(255)                NOT NULL,
    password                   VARCHAR(255)                NOT NULL,
    telephone                  VARCHAR(15),
    cpf                        VARCHAR(14),
    role_id                    UUID,
    is_system_admin            BOOLEAN,
    created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_login                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    token_version              INTEGER                     NOT NULL,
    refresh_token_hash         VARCHAR(60),
    refresh_token_expiry       TIMESTAMP WITHOUT TIME ZONE,
    refresh_token_family_id    VARCHAR(255),
    is_active                  BOOLEAN                     NOT NULL,
    is_locked                  BOOLEAN                     NOT NULL,
    failed_login_attempts      INTEGER                     NOT NULL,
    locked_until               TIMESTAMP WITHOUT TIME ZONE,
    last_password_change       TIMESTAMP WITHOUT TIME ZONE,
    password_expiry_days       INTEGER,
    require_password_change    BOOLEAN                     NOT NULL,
    mfa_enabled                BOOLEAN                     NOT NULL,
    mfa_secret                 VARCHAR(32),
    mfa_backup_codes           VARCHAR(255),
    terms_accepted             BOOLEAN                     NOT NULL,
    terms_accepted_at          TIMESTAMP WITHOUT TIME ZONE,
    terms_version              VARCHAR(255),
    privacy_policy_accepted    BOOLEAN                     NOT NULL,
    privacy_policy_accepted_at TIMESTAMP WITHOUT TIME ZONE,
    privacy_policy_version     VARCHAR(255),
    marketing_consent          BOOLEAN                     NOT NULL,
    marketing_consent_at       TIMESTAMP WITHOUT TIME ZONE,
    data_sharing_consent       BOOLEAN                     NOT NULL,
    is_deleted                 BOOLEAN                     NOT NULL,
    deleted_at                 TIMESTAMP WITHOUT TIME ZONE,
    deletion_reason            VARCHAR(255),
    anonymized                 BOOLEAN                     NOT NULL,
    anonymized_at              TIMESTAMP WITHOUT TIME ZONE,
    data_retention_until       TIMESTAMP WITHOUT TIME ZONE,
    ip_address_registration    VARCHAR(45),
    last_ip_address            VARCHAR(45),
    user_agent_registration    VARCHAR(500),
    last_user_agent            VARCHAR(500),
    geo_location_registration  VARCHAR(100),
    email_verified             BOOLEAN                     NOT NULL,
    email_verification_token   VARCHAR(255),
    email_verification_sent_at TIMESTAMP WITHOUT TIME ZONE,
    phone_verified             BOOLEAN                     NOT NULL,
    rural                      BOOLEAN                     NOT NULL,
    code                       VARCHAR(9),
    municipality               VARCHAR(255),
    location_name              VARCHAR(255),
    street                     VARCHAR(255),
    address_number             INTEGER,
    neighborhood               VARCHAR(255),
    landmark                   VARCHAR(500),
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_cpf UNIQUE (cpf);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_telephone UNIQUE (telephone);

CREATE INDEX idx_cpf ON users (cpf);

CREATE INDEX idx_deleted ON users (is_deleted);

CREATE INDEX idx_email ON users (email);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);