CREATE TABLE faqs
(
    id            UUID                        NOT NULL,
    question      TEXT                        NOT NULL,
    answer        TEXT                        NOT NULL,
    faq_category  VARCHAR(255)                NOT NULL,
    display_order INTEGER,
    is_active     BOOLEAN                     NOT NULL,
    view_count    INTEGER,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_faqs PRIMARY KEY (id)
);

CREATE TABLE support_message_attachments
(
    message_id     UUID NOT NULL,
    attachment_url VARCHAR(255)
);

CREATE TABLE support_messages
(
    id          UUID                        NOT NULL,
    ticket_id   UUID                        NOT NULL,
    user_id     UUID                        NOT NULL,
    message     TEXT                        NOT NULL,
    is_internal BOOLEAN                     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_support_messages PRIMARY KEY (id)
);

ALTER TABLE support_messages
    ADD CONSTRAINT FK_SUPPORT_MESSAGES_ON_TICKET FOREIGN KEY (ticket_id) REFERENCES support_tickets (id);

ALTER TABLE support_messages
    ADD CONSTRAINT FK_SUPPORT_MESSAGES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE support_message_attachments
    ADD CONSTRAINT fk_support_message_attachments_on_support_message FOREIGN KEY (message_id) REFERENCES support_messages (id);

CREATE TABLE support_tickets
(
    id                  UUID                        NOT NULL,
    user_id             UUID                        NOT NULL,
    subject             VARCHAR(255)                NOT NULL,
    description         TEXT                        NOT NULL,
    ticket_category     VARCHAR(255)                NOT NULL,
    ticket_priority     VARCHAR(255)                NOT NULL,
    assigned_to_user_id UUID,
    deleted_at          TIMESTAMP WITHOUT TIME ZONE,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_support_tickets PRIMARY KEY (id)
);

ALTER TABLE support_tickets
    ADD CONSTRAINT FK_SUPPORT_TICKETS_ON_ASSIGNED_TO_USER FOREIGN KEY (assigned_to_user_id) REFERENCES users (id);

ALTER TABLE support_tickets
    ADD CONSTRAINT FK_SUPPORT_TICKETS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);