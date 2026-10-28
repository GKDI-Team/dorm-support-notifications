# dorm-support-notifications

## API documentation

This service has no public business HTTP endpoints in the MVP. Swagger UI is available at `/swagger-ui.html` for the health endpoint.

The Kafka event contract consumed by this service is stored in `src/main/resources/events.schema.json`.

## Local database initialization

The schema is stored in `src/main/resources/db/init.sql` and is mounted by the root
`docker-compose.yml` into `/docker-entrypoint-initdb.d/`.
PostgreSQL executes this script automatically when the `notifications_db` volume is
created for the first time. A regular `git push` does not modify an existing database
volume. If the volume already exists, apply the script once manually or recreate only
the notifications database volume when it is safe to remove local data.

## Kafka MVP

The service consumes `dormsupport.issue-events` and writes one notification record per
event to `notifications_db`. Duplicate events are ignored by `event_id`. At this stage
there is no Telegram delivery: every accepted event is saved with delivery status
`SKIPPED` and the received event is written to the service log.
