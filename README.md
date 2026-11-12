# Grocery Spending Analytics

Spring Boot service that consumes grocery spending events from Amazon SQS, stores them in PostgreSQL, and exposes the current month's total through a REST API.

## Requirements

- Java 17 or newer
- PostgreSQL with an `analytics` database
- An Amazon SQS queue and AWS credentials with permission to consume its messages

The project uses Spring Boot 4.1.0, Spring Cloud AWS, JDBC, and Flyway. The Maven wrapper is included.

## Configuration

Set credentials in your shell before starting the application:

```sh
export DB_PASSWORD='your-database-password'
export AWS_ACCESS_KEY_ID='your-access-key-id'
export AWS_SECRET_ACCESS_KEY='your-secret-access-key'
```

Do not commit real credentials. The defaults in `src/main/resources/application.properties` are:

| Setting | Default | Environment override |
| --- | --- | --- |
| HTTP port | `8082` | `SERVER_PORT` |
| Database URL | `jdbc:postgresql://localhost:5433/analytics` | `SPRING_DATASOURCE_URL` |
| Database user | `postgres` | `SPRING_DATASOURCE_USERNAME` |
| AWS region | `us-east-1` | `SPRING_CLOUD_AWS_REGION_STATIC` |
| SQS queue | `analytics-groceries-queue` | `AWS_SQS_ANALYTICS_QUEUE` |

Create the database and queue before starting. Flyway creates the `analytics` table automatically.

## Run

```sh
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Event format

Send a JSON message to the configured SQS queue:

```json
{"date": "2026-11-12", "amount": 42.50}
```

The listener also accepts `checkoutDate` and `totalPrice` as field names, and supports an SNS envelope whose `Message` field contains the JSON event. Dates use `YYYY-MM-DD`; both date and amount are required.

Each received event inserts a row. Duplicate deliveries are not deduplicated.

## API

```sh
curl http://localhost:8082/api/groceries/spending/current-month
```

Example response:

```json
{"month": "2026-11", "amount": 42.50}
```

The month follows the application's system time zone. If no spending is recorded for the current month, the amount is zero.

## Build and test

```sh
./mvnw -DskipTests package
./mvnw test
```

The context-loading test requires the configured PostgreSQL database, SQS queue, and AWS credentials.
