DROP INDEX idx_events_status_starts_at;
CREATE INDEX idx_events_status_ends_at ON events (status, ends_at);