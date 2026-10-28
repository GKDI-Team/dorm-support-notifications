CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    issue_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    previous_status VARCHAR(30),
    occurred_at TIMESTAMPTZ NOT NULL,
    title VARCHAR(150) NOT NULL,
    description_preview VARCHAR(500),
    block VARCHAR(50),
    room_number VARCHAR(20),
    location_details VARCHAR(250),
    category_name VARCHAR(80),
    delivery_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    delivery_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ,
    CONSTRAINT uk_notifications_event_id UNIQUE (event_id),
    CONSTRAINT ck_notifications_status CHECK (status IN ('NEW', 'IN_PROGRESS', 'CLOSED')),
    CONSTRAINT ck_notifications_previous_status CHECK (
        previous_status IS NULL OR previous_status IN ('NEW', 'IN_PROGRESS', 'CLOSED')
    ),
    CONSTRAINT ck_notifications_delivery_status CHECK (
        delivery_status IN ('PENDING', 'SENT', 'SKIPPED', 'FAILED')
    )
);

CREATE INDEX IF NOT EXISTS ix_notifications_issue_id ON notifications (issue_id);
CREATE INDEX IF NOT EXISTS ix_notifications_created_at ON notifications (created_at DESC);
