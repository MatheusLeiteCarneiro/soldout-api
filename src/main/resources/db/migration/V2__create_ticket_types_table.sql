CREATE TABLE ticket_types
(
    id                 UUID           NOT NULL,
    event_id           UUID           NOT NULL,
    name               VARCHAR(100)   NOT NULL,
    description        VARCHAR(500)   NOT NULL,
    price              NUMERIC(10, 2) NOT NULL,
    total_quantity     INTEGER        NOT NULL,
    available_quantity INTEGER        NOT NULL,

    CONSTRAINT pk_ticket_types PRIMARY KEY (id),

    CONSTRAINT fk_ticket_types_event FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE CASCADE,

    CONSTRAINT chk_ticket_types_price_not_negative CHECK (price >= 0),

    CONSTRAINT chk_ticket_types_total_positive CHECK (total_quantity > 0),

    CONSTRAINT chk_ticket_types_available_not_negative CHECK (available_quantity >= 0),

    CONSTRAINT chk_ticket_types_available_within_total CHECK (available_quantity <= total_quantity)
);

CREATE INDEX idx_ticket_types_event_id ON ticket_types (event_id);