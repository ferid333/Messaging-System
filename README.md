# Messaging Chat Service

Backend service for a chat/messaging system. It provides REST APIs for conversations, messages, read/delivery state, and attachment upload flow, plus STOMP over WebSocket for real-time message delivery.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Build tool | Gradle |
| REST API | Spring Web / Spring MVC |
| WebSocket | Spring WebSocket + STOMP + SockJS |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 16 |
| Migrations | Liquibase |
| Cache / WebSocket tickets | Redis |
| Messaging | RabbitMQ, Spring AMQP |
| Object storage | AWS S3 SDK v2 |
| API documentation | springdoc-openapi / Swagger UI |
| Mapping | MapStruct |
| Validation | Jakarta Bean Validation |
| Boilerplate reduction | Lombok |
| Testing | Spock, Groovy, Spring Boot Test |
| Containerization | Docker, Docker Compose |
| Code quality | Checkstyle |

## Main Features

- Create conversations with multiple participants.
- List conversations for a user with cursor pagination.
- Send text, image, or file messages.
- Fetch messages for a conversation with pagination.
- Track delivered and read message state.
- Send and receive chat events over WebSocket/STOMP.
- Generate S3 presigned upload URLs for attachments.
- Confirm uploaded attachments by checking S3 object metadata.
- Publish message notification events to RabbitMQ after database commit.
- Manage schema changes through Liquibase migrations.

## Project Structure

```text
.
+-- src/main/java/com/messaging/chat
|   +-- config        # WebSocket, RabbitMQ, S3, OpenAPI configuration
|   +-- controller    # REST and WebSocket controllers
|   +-- dao           # JPA entities and repositories
|   +-- mapper        # MapStruct mappers
|   +-- model         # DTOs, constants, exceptions
|   +-- service       # Business logic
|   +-- util          # Utility classes
+-- src/main/resources
|   +-- application.yaml
|   +-- db/changelog  # Liquibase changelogs
+-- build.gradle
+-- Dockerfile
+-- docker-compose.yml
```

## Prerequisites

- Java 21
- Docker and Docker Compose
- Gradle wrapper included in the project
- Optional: an AWS S3 bucket, LocalStack, or MinIO-compatible setup for attachment upload testing

## Running With Docker Compose

Build and start all Compose services:

```bash
docker compose up --build
```

The Compose file starts:

| Service | URL / Port |
| --- | --- |
| Chat service | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |
| RabbitMQ AMQP | `localhost:5672` |
| RabbitMQ Management UI | `http://localhost:15672` |

RabbitMQ default credentials:

```text
guest / guest
```

Stop services:

```bash
docker compose down
```

Stop services and remove volumes:

```bash
docker compose down -v
```

## Running Locally Without Dockerizing the App

Start infrastructure only:

```bash
docker compose up postgres redis rabbitmq
```

Then run the app from the project root:

```bash
./gradlew bootRun
```

On Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

By default, `application.yaml` points PostgreSQL to `localhost:5432`. When the app runs inside Docker, `docker-compose.yml` overrides this to use the Compose service name `postgres`.

## Configuration

Spring Boot loads configuration from `src/main/resources/application.yaml`. Environment variables from Docker Compose override those values at startup through Spring Boot externalized configuration.

For example:

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/chat
```

maps to:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/chat
```

Important environment variables used by `chat-service`:

| Environment variable | Spring property | Purpose |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `spring.datasource.username` | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` | PostgreSQL password |
| `SPRING_RABBITMQ_HOST` | `spring.rabbitmq.host` | RabbitMQ host |
| `SPRING_RABBITMQ_PORT` | `spring.rabbitmq.port` | RabbitMQ port |
| `SPRING_RABBITMQ_USERNAME` | `spring.rabbitmq.username` | RabbitMQ username |
| `SPRING_RABBITMQ_PASSWORD` | `spring.rabbitmq.password` | RabbitMQ password |
| `SPRING_DATA_REDIS_HOST` | `spring.data.redis.host` | Redis host |
| `SPRING_DATA_REDIS_PORT` | `spring.data.redis.port` | Redis port |
| `STORAGE_S3_BUCKET_NAME` | `storage.s3.bucket-name` | S3 bucket for attachments |
| `STORAGE_S3_REGION` | `storage.s3.region` | S3 region |
| `STORAGE_S3_ACCESS_KEY_ID` | `storage.s3.access-key-id` | S3 access key |
| `STORAGE_S3_SECRET_ACCESS_KEY` | `storage.s3.secret-access-key` | S3 secret key |
| `RABBITMQ_NOTIFICATION_QUEUE` | `rabbitmq.notification-queue` | Notification queue name |
| `RABBITMQ_NOTIFICATION_EXCHANGE` | `rabbitmq.notification-exchange` | Notification exchange name |
| `RABBITMQ_NOTIFICATION_ROUTING_KEY` | `rabbitmq.notification-routing-key` | Notification routing key |
| `RABBITMQ_NOTIFICATION_DLQ` | `rabbitmq.notification-dlq` | Dead-letter queue name |
| `RABBITMQ_NOTIFICATION_DLX` | `rabbitmq.notification-dlx` | Dead-letter exchange name |

Note: `docker-compose.yml` does not currently start an S3-compatible service. The S3 values in Compose are placeholders unless you connect them to a real S3 bucket or add a local S3-compatible service such as LocalStack or MinIO.

## API Documentation

When the app is running, OpenAPI documentation is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The generated OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

## REST API Overview

All REST endpoints use the `userId` request header to identify the current user.

### Conversations

Create a conversation:

```http
POST /ms-chat/conversations
userId: 1
Content-Type: application/json

{
  "participantUserIds": [2, 3]
}
```

List conversations:

```http
GET /ms-chat/conversations?limit=20&cursor=50
userId: 1
```

List messages in a conversation:

```http
GET /ms-chat/conversations/20/messages?limit=30&beforeMessageId=171
userId: 1
```

Send a message:

```http
POST /ms-chat/conversations/20/messages
userId: 1
Content-Type: application/json

{
  "clientMessageId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "TEXT",
  "textContent": "Hello",
  "attachmentIds": []
}
```

Mark messages delivered:

```http
POST /ms-chat/conversations/20/delivery
userId: 1
Content-Type: application/json

{
  "messageId": 100
}
```

Mark messages read:

```http
POST /ms-chat/conversations/20/read
userId: 1
Content-Type: application/json

{
  "messageId": 100
}
```

Supported message types:

```text
TEXT, IMAGE, FILE
```

### Attachments

Create a presigned upload URL:

```http
POST /ms-chat/attachments/presign-upload
userId: 1
Content-Type: application/json

{
  "fileName": "photo.png",
  "contentType": "image/png",
  "fileSize": 1048576,
  "checksum": "1B2M2Y8AsgTpgAmY7PhCfg=="
}
```

The response contains:

- `attachmentId`
- `storageKey`
- `uploadUrl`
- HTTP method, currently `PUT`
- expiration time
- required upload headers

After uploading the file to S3 with the returned URL, confirm the attachment:

```http
POST /ms-chat/attachments/10/confirm
userId: 1
```

Attachment rules:

- Maximum file size is less than 5 MB.
- Executable content types are rejected.
- Executable file extensions are rejected.
- Confirmation checks that the uploaded S3 object size and content type match the presigned request.

## WebSocket / STOMP

WebSocket endpoint:

```text
/ws-chat
```

SockJS is enabled.

The connection requires a query parameter:

```text
/ws-chat?ticket=<ticket>
```

The ticket is resolved from Redis using this key format:

```text
chat:ws-ticket:<ticket>
```

The Redis value must be the user id. The ticket is consumed with `GETDEL`, so it is single-use.

Application destination prefix:

```text
/app
```

User destination prefix:

```text
/user
```

Client sends messages to:

| Action | STOMP destination |
| --- | --- |
| Send message | `/app/conversations/{conversationId}/messages` |
| Mark delivered | `/app/conversations/{conversationId}/delivery` |
| Mark read | `/app/conversations/{conversationId}/read` |

Client subscribes to user queues:

| Event | STOMP subscription |
| --- | --- |
| New messages | `/user/queue/messages` |
| Delivery updates | `/user/queue/delivery` |
| Read updates | `/user/queue/read` |

## RabbitMQ Notifications

When a message is sent, the service publishes notification events after the database transaction commits.

Configured RabbitMQ components:

- Notification queue
- Topic exchange
- Routing key binding
- Dead-letter queue
- Dead-letter exchange

The queue and exchange names are configured through `rabbitmq.notification-*` properties.

## Database Migrations

Liquibase runs automatically on application startup.

Master changelog:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

The application uses:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

That means Hibernate does not create/update schema automatically; schema changes should be added through Liquibase changelogs.

## Build, Test, and Quality Checks

Build the project:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

Run Checkstyle:

```bash
./gradlew checkstyleMain checkstyleTest
```

Create the Spring Boot jar:

```bash
./gradlew bootJar
```

On Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`.

## Docker Image

The `Dockerfile` uses a multi-stage build:

1. `eclipse-temurin:21-jdk` builds the Spring Boot jar.
2. `eclipse-temurin:21-jre` runs the jar.

The app listens on port `8080`.

## Useful Local URLs

| Tool | URL |
| --- | --- |
| Application | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Actuator health | `http://localhost:8080/actuator/health` |
| RabbitMQ UI | `http://localhost:15672` |

## Development Notes

- REST requests use the `userId` header instead of a full authentication system.
- WebSocket authentication uses a single-use Redis ticket.
- S3 presigned upload URLs expire after `storage.s3.presign-duration-minutes`, currently configured as `5`.
- Message notification publishing is transactional: RabbitMQ publishing runs after the database commit.
- Docker Compose service names are used for container-to-container networking, for example `postgres`, `redis`, and `rabbitmq`.
