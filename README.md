# ReserveIT Backend

> Spring Boot backend for the ReserveIT restaurant reservation platform. It exposes REST and WebSocket APIs for customers, restaurant staff, and platform admins to manage companies, dining tables, reservations, and users.

## Table of Contents
- [Overview](#overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Domain Model](#domain-model)
- [Runtime Profiles & Configuration](#runtime-profiles--configuration)
- [Local Development](#local-development)
- [Testing & Quality Gates](#testing--quality-gates)
- [Container Image](#container-image)
- [API Surface Overview](#api-surface-overview)
- [Realtime & Background Work](#realtime--background-work)
- [Project Layout](#project-layout)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## Overview
ReserveIT Backend is a Java 17 / Spring Boot 3 service that powers the restaurant reservation experience:

- Customers browse partner restaurants, create and manage reservations, and track their bookings.
- Restaurant managers design the dining room layout, configure tables, and monitor live occupancy.
- Staff users check guests in/out, mark no-shows, and receive proactive alerts.
- Platform admins control access, onboard venues, and audit usage.

The service exposes secure REST endpoints, pushes live updates over STOMP/WebSocket, and integrates with SMTP for transactional emails. Production deployments target MySQL 8+, while automated tests run against an isolated H2 database.

## Key Features
- **Role-aware access control** – `CUSTOMER`, `STAFF`, `MANAGER`, and `ADMIN` roles enforced through `SecurityConfig` and method-level `@PreAuthorize`.
- **JWT authentication** – stateless access tokens plus HTTP-only refresh cookies, password hashing via BCrypt, and refresh token persistence.
- **Reservation workflow** – create/modify/cancel, quick reservations, availability checks, check-in/out, mark no-shows, extend stays, and upcoming lists.
- **Smart table allocation** – `TableAllocationService` picks optimal tables based on party size, availability, and peak-hour penalties.
- **Drag-and-drop floor plans** – store table geometry (position, rotation, shape, floor, indoor/outdoor) per company and broadcast updates over WebSockets.
- **Company + staff management** – CRUD for restaurants, staff rosters, and dashboard stats (available vs. occupied tables, reservation counts).
- **Realtime collaboration** – STOMP topics broadcast reservation/table events to targeted restaurants (`/topic/tables/{companyId}`, `/topic/reservations/{companyId}`, `/topic/notifications/{companyId}`).
- **Operational automation** – scheduled tasks detect late arrivals, upcoming seatings, and extended stays, nudging staff via WebSocket notifications.
- **Observability** – Spring Boot Actuator health, structured logging (`lombok @Slf4j`), Jacoco coverage, and SonarQube hooks.
- **Email workflows** – transactional messages for credentials, onboarding, and password reset via `spring-boot-starter-mail`.

## Tech Stack
- **Runtime**: Java 17, Spring Boot 3.1 (Web, Security, Data JPA, Validation, Actuator, Mail, WebSocket, Scheduling)
- **Persistence**: MySQL 8 (prod/dev), H2 (tests), Spring Data JPA repositories, Jakarta Validation
- **Security**: JWT (`io.jsonwebtoken`), BCrypt password hashing, custom `JwtAuthFilter`
- **Messaging**: Spring WebSocket + STOMP + SockJS
- **Build & Quality**: Gradle Kotlin DSL, JUnit 5, Mockito, Jacoco, SonarQube plugin, Docker multi-stage build

## Architecture
```
┌────────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐
│  Controllers├──►│   Services   ├──► │  Database    │──► │  Repos /   │
│(REST & WS)  │    │(business logic│   │facades (DB) │    │  Entities  │
└────────────┘    └──────────────┘    └──────────────┘    └────────────┘
        │                   │                    │                 │
        └────────────► Utilities (JWT, schedulers, allocation, mail)
```
- **Controllers (`com.reserveit.controller`)** expose REST endpoints per domain (`Auth`, `Company`, `Reservation`, `Table`, `Staff`, `Admin`, `User`).
- **Services (`logic.impl`)** contain orchestration (reservation lifecycle, company onboarding, notifications, etc.).
- **Database facades (`database.impl`)** wrap Spring Data repositories, providing a separation layer.
- **Entities (`model`)** map to relational tables and own domain-specific behavior (status transitions, validations).
- **Utilities (`util`)** provide cross-cutting helpers like JWT generation, password hashing, schedulers, and table allocation heuristics.

## Domain Model
- **User** – root entity for all accounts (customers, staff, managers, admins) with contact details, role, and hashed password.
- **Staff** – extends `User`, links to a `Company`, restricted to `MANAGER`/`STAFF` roles.
- **Company** – restaurant metadata (name, address, contact, rating, photo) plus relationships to `Category`, `DiningTable`, `Staff`, and `Reservation`.
- **DiningTable** – capacity, layout coordinates, floor, shape, status (`AVAILABLE`, `RESERVED`, `OCCUPIED`, `OUT_OF_SERVICE`), and optional `TableConfiguration`.
- **Reservation** – party size, special requests, start/end times, `ReservationStatus` (CONFIRMED, ARRIVED, COMPLETED, CANCELLED, NO_SHOW, etc.), ties to user, company, and table.
- **RefreshToken** – persistent token per user for refresh flow security.

## Runtime Profiles & Configuration
| Profile | File | Purpose |
|---------|------|---------|
| `default` | `src/main/resources/application.properties` | Baseline MySQL + mail + JWT config used in production-like environments. |
| `dev` | `src/main/resources/application-dev.properties` | Developer profile; same defaults but can be overridden via env vars. |
| `test` | `src/test/resources/application-test.properties` | Uses in-memory H2, auto `create-drop` schema, safe mail/JWT defaults. |

### Required Environment Variables
Set these via shell export, `.env`, IntelliJ run configuration, or your container orchestrator:

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL for MySQL schema | `jdbc:mysql://localhost:3306/reserveit_db?useSSL=false&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | DB credentials | `reserveit` / `superSecret` |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | SMTP settings for transactional email | Gmail/App Password |
| `JWT_SECRET` | Base64 string used to sign access tokens | `base64encodedsecret` |
| `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION` | Token TTLs in ms | `3600000`, `86400000` |
| `SONAR_HOST_URL`, `SONAR_TOKEN` | (Optional) SonarQube analysis | `http://localhost:9000`, `XXXXXXXX` |

_Tip:_ never commit raw secrets (the sample values in `application*.properties` are placeholders only).

## Local Development
### Prerequisites
- JDK 17+
- Gradle 8.x (or use the included `./gradlew`)
- MySQL 8.x
- (Optional) Docker Desktop for container builds

### Database bootstrap
1. Start MySQL locally and create the schema:
   ```sql
   CREATE DATABASE reserveit_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Create a user with appropriate privileges or reuse `root`.
3. Update environment variables (or the `application-dev.properties`) to point to the local DB.
4. On first run Spring JPA (`spring.jpa.hibernate.ddl-auto=update`) materializes the schema; adjust if you prefer migrations.

### Run the API
```bash
# Windows PowerShell
.\gradlew bootRun --args="--spring.profiles.active=dev"

# macOS/Linux
./gradlew bootRun --args='--spring.profiles.active=dev'
```
The service listens on `http://localhost:8080`. CORS already trusts the front-end default origins (`http://localhost:5200`, `http://127.0.0.1:5200`, `http://172.29.96.1:5200`).

### Common Gradle commands
```bash
./gradlew clean            # remove previous build artifacts
./gradlew build            # compile + unit tests + package (disables plain jar)
./gradlew bootJar          # create executable jar (output: build/libs/app.jar)
./gradlew bootRun          # run with local profile overrides
```

## Testing & Quality Gates
- **Unit tests** – `./gradlew test` (auto-activates H2 profile, no external DB needed).
- **Coverage** – `./gradlew jacocoTestReport` outputs XML + HTML under `build/reports/jacoco/test`.
- **Static analysis** – `./gradlew sonar -Dsonar.token=<token> -Dsonar.host.url=<url>` (Sonar plugin is preconfigured for project key `reserveIT2.0`). `sonar` and legacy `sonarqube` tasks both depend on Jacoco.
- **Actuator smoke check** – `curl http://localhost:8080/actuator/health`.

CI/CD pipelines (see `.gitlab-ci.yml`) can reuse the same commands. HTML coverage reports are stored in `build/reports/jacoco/test/html/index.html`.

## Container Image
Multi-stage Dockerfile (`Dockerfile`) builds a slim runtime image:
```bash
docker build -t reserveit-backend .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/reserveit_db \
  -e SPRING_DATASOURCE_USERNAME=reserveit \
  -e SPRING_DATASOURCE_PASSWORD=*** \
  reserveit-backend
```
For SonarQube in Docker, optional compose files (`docker-compose.sonar*.yml`) stand up the scanner + database.

## API Surface Overview
The full API is documented through the controllers (see `src/main/java/com/reserveit/controller`). Key entry points:

| Area | Base Path | Highlights |
|------|-----------|------------|
| **Authentication** | `/api/auth` | Register, login, refresh token (reads `refreshToken` cookie), logout. Responses include access token, role, and optional `companyId`. |
| **Users** | `/api/users` | Lookup by ID/email, list users, admin-only delete. |
| **Admin** | `/api/admin` | Admin-only user provisioning with autogenerated credentials (and email notifications). |
| **Companies** | `/api/companies` | CRUD, dashboard stats (`/dashboard`), table layout retrieval. |
| **Tables** | `/api/tables` | CRUD, status updates, batch position saves, allocation helpers, plus websocket notifications on changes. |
| **Reservations** | `/api/reservations` | CRUD, quick bookings, `my-reservations`, status filtering, availability checks, bulk delete (admin tooling). |
| **Staff Reservations** | `/api/staff/reservations` | Staff/manager-only endpoints for check-in/out, mark no-show, pending arrivals, extended stays, per-company views. |
| **WebSocket** | `/ws` (handshake), `/topic/*` | Realtime updates for tables, reservations, and staff notifications. Connect via STOMP/SockJS. |
| **Health** | `/actuator/health` | Liveness/readiness for deployment targets. |

> Detailed request/response payloads live in the DTOs (e.g., `ReservationDto`, `TablePositionDto`, `CompanyDto`, `UserDto`) under `src/main/java/com/reserveit/dto`.

## Realtime & Background Work
- **WebSockets** – `WebSocketConfig` registers `/ws` endpoint with SockJS fallback. `WebSocketServiceImpl` publishes to:
  - `/topic/tables/{companyId}` when a table changes.
  - `/topic/reservations/{companyId}` whenever a reservation is created/updated.
  - `/topic/notifications/{companyId}` for staff alerts (late arrivals, table status changes).
- **Schedulers** – `ReservationScheduler` runs every minute to:
  - detect late arrivals and push staff notifications,
  - pre-reserve tables for parties arriving within 30 minutes,
  - alert when extended stays occupy a table longer than planned.
- **Email flows** – `EmailServiceImpl` sends onboarding credentials, admin alerts, and password reset tokens using the configured SMTP server.

## Project Layout
```
├── build.gradle.kts              # Gradle configuration (plugins, deps, Jacoco, Sonar)
├── Dockerfile                    # Multi-stage build for production images
├── src
│   ├── main
│   │   ├── java/com/reserveit
│   │   │   ├── MainApplication.java
│   │   │   ├── config/           # Security, WebSocket, Mail configuration beans
│   │   │   ├── controller/       # REST controllers grouped by domain
│   │   │   ├── logic/impl|interfaces
│   │   │   ├── database/         # Persistence layer wrappers
│   │   │   ├── repository/       # Spring Data JPA repositories
│   │   │   ├── model/            # Entities (Company, DiningTable, Reservation, User, etc.)
│   │   │   ├── dto/              # API payload contracts
│   │   │   ├── enums/            # Status + role enumerations
│   │   │   └── util/             # JWT, schedulers, password hashing, allocation helpers
│   │   └── resources
│   │       └── application*.properties
│   └── test                      # JUnit + Mockito tests, H2 config
└── .gitlab-ci.yml                # Pipeline (build, test, sonar) definition
```

## Troubleshooting
- **Authentication fails immediately** – ensure `JWT_SECRET` is ≥32 bytes (base64) and matches for all replicas; verify clocks are in sync for expiration checks.
- **Tables/reservations not updating live** – confirm the front-end subscribes to `/topic/tables/{companyId}` and WebSocket origin matches the allowed list; inspect server logs for STOMP errors.
- **Emails not delivered** – double-check SMTP credentials, TLS requirements, and whether your provider blocks less-secure apps; `spring.mail.properties.mail.smtp.starttls.enable=true` is required for Gmail.
- **MySQL schema drift** – Hibernate auto-update is convenient but not a substitute for migrations. For production, consider Liquibase/Flyway and set `spring.jpa.hibernate.ddl-auto=validate`.
- **Sonar task fails** – the Gradle plugin expects `SONAR_HOST_URL`/`SONAR_TOKEN`; if unavailable, skip the `sonar` task or run `./gradlew build` alone.

## Contributing
1. Fork / branch from `main`.
2. Keep code style consistent (Java 17, Lombok-enabled, prefer constructor injection).
3. Add unit tests around new logic (H2 profile makes this fast).
4. Run `./gradlew clean test jacocoTestReport`.
5. Submit a merge request summarizing behavioral changes and testing evidence.

For questions or pairing, reach out via the course communication channel or project issue tracker.
