# dorm-support-notifications

## О сервисе

Сервис отправки уведомлений о заявках в Telegram.

**Технологии:** Java, Spring Boot, PostgreSQL, Kafka, Telegram Bot API.

**Основные ручки:** публичных ручек управления уведомлениями нет; сервис получает события из Kafka.

## Запуск

Из папки `dorm-support-infrastructure`:

```bash
docker compose up --build notifications-service
```

Для отправки в Telegram задайте переменные окружения `TELEGRAM_BOT_TOKEN` и `TELEGRAM_CHAT_ID`. Если Telegram не нужен, задайте `TELEGRAM_ENABLED=false`. Compose также запустит PostgreSQL и Kafka. Весь проект можно поднять командой `make up` из папки инфраструктуры.

## Документация API

В текущей версии сервис не предоставляет публичных ручек для работы с уведомлениями. Swagger UI для ручки проверки состояния доступен по адресу `/swagger-ui.html`.

Описание получаемых сервисом событий Kafka находится в `src/main/resources/events.schema.json`.

## Инициализация локальной базы данных

Схема хранится в `src/main/resources/db/init.sql` и подключается через
`docker-compose.yml` в `/docker-entrypoint-initdb.d/`. PostgreSQL выполняет скрипт
автоматически при первом создании тома `notifications_db`. Команда `git push` не
изменяет существующий том базы данных. Если том уже существует, примените скрипт
вручную или пересоздайте только том базы уведомлений, когда удаление локальных данных безопасно.

## Kafka

Сервис получает события из `dormsupport.issue-events` и сохраняет уведомления в
`notifications_db`. Отправка в Telegram настраивается через переменные окружения.
