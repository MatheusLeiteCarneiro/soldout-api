CREATE TABLE events (
                        id          UUID          NOT NULL,
                        name        VARCHAR(150)  NOT NULL,
                        description VARCHAR(2500) NOT NULL,
                        starts_at   TIMESTAMPTZ   NOT NULL,
                        ends_at     TIMESTAMPTZ   NOT NULL,
                        status      VARCHAR(20)   NOT NULL,

                        CONSTRAINT pk_events PRIMARY KEY (id),

                        CONSTRAINT chk_events_status CHECK (
                            status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'FINISHED')
                            ),

                        CONSTRAINT chk_events_period CHECK (ends_at > starts_at)
);

CREATE INDEX idx_events_status_starts_at ON events (status, starts_at);