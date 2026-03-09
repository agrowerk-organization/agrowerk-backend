CREATE TABLE file_metadata
(
    id                   UUID                        NOT NULL,
    cloudinary_public_id VARCHAR(255)                NOT NULL,
    original_url         VARCHAR(500)                NOT NULL,
    thumbnail_url        VARCHAR(500),
    medium_url           VARCHAR(500),
    original_file_name   VARCHAR(255)                NOT NULL,
    content_type         VARCHAR(255)                NOT NULL,
    file_size            BIGINT                      NOT NULL,
    width                INTEGER,
    height               INTEGER,
    file_category        VARCHAR(255)                NOT NULL,
    entity_id            UUID,
    deleted              BOOLEAN                     NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at           TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_file_metadata PRIMARY KEY (id)
);

ALTER TABLE file_metadata
    ADD CONSTRAINT uc_file_metadata_cloudinarypublicid UNIQUE (cloudinary_public_id);

CREATE INDEX idx_file_deleted ON file_metadata (deleted);

CREATE INDEX idx_file_entity ON file_metadata (entity_id, file_category);