# TalentHub

A freelance job board platform built for the SoftUni **Spring Advanced** course (June 2026) individual project. Clients post jobs, freelancers apply, and the platform tracks job-category statistics via a dedicated REST microservice.

## Tech Stack

### Main Application (`main-app`, port 8080)
- **Java** 17 / **Spring Boot** 3.4.0
- Spring MVC + Thymeleaf (server-rendered UI)
- Spring Data JPA + Hibernate
- Spring Security (dual filter chain: session for UI, JWT for `/api/**`)
- Spring Cache backed by **Redis**
- Spring Cloud OpenFeign (inter-service communication)
- Spring AOP (`@Around` service execution logging aspect)
- Spring Events (`ApplicationEvent` + `@EventListener`)
- Spring HATEOAS (client-side consumption of hypermedia responses)
- Flyway (database migrations)
- **JWT** (`jjwt 0.12`) — stateless token issuance via `POST /api/auth/login`
- **OpenPDF** + **Apache POI** — PDF and Excel export
- **ExchangeRate-API** (free, no key) — live budget currency conversion
- Internationalization (i18n) — English + Bulgarian via `?lang=` cookie param
- MariaDB, Lombok, Maven

### Stats Microservice (`stats-svc`, port 8081)
- **Java** 17 / **Spring Boot** 3.4.0
- Spring Web + **Spring HATEOAS** (HAL responses on all endpoints)
- Spring Data JPA + Hibernate
- Spring Cache backed by **Redis**
- Spring AOP (`@Around` service execution logging aspect)
- Flyway, MariaDB, Lombok, Maven

## Architecture

```
[ Browser ] ──► [ main-app :8080 ] ──Feign──► [ stats-svc :8081 ]
                      │                               │
               talenthub_db                  talenthub_stats_db
                      │                               │
              Redis :6379 ◄─────────────────────────►
```

Both applications run independently on separate ports with separate databases. Redis is shared for caching.

## Domain Model (Main App)

| Entity | Type | Notes |
|---|---|---|
| `User` | technical | Auth entity — username, email, hashed password, role, permissions |
| `Permission` | technical | Fine-grained permission (e.g. `EXPORT_DATA`) granted to users by an admin |
| `JobPost` | domain #1 | Created by a CLIENT; has title, category, budget, status |
| `Application` | domain #2 | A FREELANCER applying to a JobPost with cover letter and rate |
| `Review` | domain #3 | A CLIENT reviewing a FREELANCER after project completion |

**Entity relationships:** `Application → JobPost` (ManyToOne), `Application → User` (ManyToOne), `JobPost → User` (ManyToOne), `Review → User` × 2 (ManyToOne), `User ↔ Permission` (ManyToMany via `user_permissions`)

## Roles, Permissions and Security

### Roles
| Role | Permissions |
|---|---|
| Guest | Browse jobs, view details, register, log in |
| FREELANCER | Apply, withdraw applications, view own applications, edit profile |
| CLIENT | Post, edit, close, delete jobs; accept/reject applications; leave reviews |
| ADMIN | All of the above + manage user roles and permissions |

### Permissions (in addition to roles)
Admins can grant/revoke fine-grained permissions to any user:
- `MANAGE_JOBS` — extended job management
- `EXPORT_DATA` — access to `/api/export/**` PDF/Excel endpoints
- `VIEW_STATS` — explicit stat access
- `MANAGE_REVIEWS` — review moderation

### JWT
Issue a token via `POST /api/auth/login` with `{"username":"...", "password":"..."}`. Include in subsequent API calls as `Authorization: Bearer <token>`.

## Functionalities (Main App — 11 valid domain functionalities)

1. Create job post (CLIENT)
2. Edit job post (CLIENT)
3. Delete job post (CLIENT)
4. Close job post — changes status (CLIENT)
5. Mark job as filled — publishes `JobFilledEvent` (CLIENT)
6. Apply to a job (FREELANCER)
7. Withdraw application (FREELANCER)
8. Accept application (CLIENT)
9. Reject application (CLIENT)
10. Leave a review for a freelancer (CLIENT)
11. Delete a review (CLIENT / ADMIN)

## Functionalities (Stats Microservice — 2 valid domain functionalities)

1. `POST /api/stats` — record stat when a job post is created
2. `PUT /api/stats/{category}` — update application count when a freelancer applies

## Bonus Features Implemented

| Feature | Points | Notes |
|---|---|---|
| AOP Advices | 2 | `ServiceLoggingAspect` in both apps — `@Around` for execution time + exception logging |
| Docker | 1 | `Dockerfile` per app + `docker-compose.yml` (MariaDB × 2, Redis, stats-svc, main-app) |
| Spring Events | 1 | `JobFilledEvent` published by `JobPostService`, consumed async by `JobFilledEventListener` |
| HATEOAS | 2 | `stats-svc` returns `EntityModel` / `CollectionModel` with `_links`; main-app deserializes via `StatResponseCollection` |
| Redis Caching | 5 | Both apps use `RedisCacheManager`; `openJobs`, `allStats`, `exchangeRates` cached with TTL |
| JWT Authorization | 4 | Dual filter chains; stateless JWT for `/api/**`, session for Thymeleaf UI |
| 3rd-party REST API | 2 | `ExchangeRateClient` → `open.exchangerate-api.com` (free, no key); budget shown in USD/EUR/GBP/BGN on job details |
| Permissions | 2 | `Permission` entity; admin grants/revokes via UI; `@PreAuthorize("hasAuthority('PERMISSION_EXPORT_DATA')")` on export endpoints |
| i18n | 2 | `messages.properties` (EN) + `messages_bg.properties` (BG); language switcher in nav (`?lang=en` / `?lang=bg`) |
| PDF/Excel Export | 2 | `GET /api/export/jobs/pdf` and `/excel`; requires `EXPORT_DATA` permission + JWT token |
| **Total bonus** | **23 (capped at 15)** | |

## Running Locally

**1. Start Redis and databases:**
```bash
# Fastest way — use Docker Compose for infrastructure only
docker compose up redis mariadb-main mariadb-stats -d
```

Or start MariaDB locally and run Redis via Docker:
```bash
docker run -d -p 6379:6379 redis:7.2-alpine
mariadb -u root -p -e "CREATE DATABASE talenthub_db CHARACTER SET utf8mb4;"
mariadb -u root -p -e "CREATE DATABASE talenthub_stats_db CHARACTER SET utf8mb4;"
```

**2. Set credentials (or edit `application.yml` directly):**
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-at-least-256-bits-long
```

**3. Start stats-svc first:**
```bash
cd stats-svc && mvn spring-boot:run
```

**4. Start main-app:**
```bash
cd main-app && mvn spring-boot:run
```

**5. Visit:** `http://localhost:8080`

## Running with Docker Compose (full stack)

```bash
# Build JARs first
cd main-app  && mvn clean package -DskipTests && cd ..
cd stats-svc && mvn clean package -DskipTests && cd ..

# Bring up everything
docker compose up --build
```

## Running Tests

```bash
cd main-app  && mvn test
cd stats-svc && mvn test
```

Tests use H2 in-memory database and `spring.cache.type: simple` — no Redis or MariaDB needed.

## API Quick Reference

### JWT
```
POST /api/auth/login          {"username":"...", "password":"..."}  → {"token":"..."}
```

### Export (requires EXPORT_DATA permission + Bearer token)
```
GET  /api/export/jobs/pdf     → talenthub-jobs.pdf
GET  /api/export/jobs/excel   → talenthub-jobs.xlsx
```

### Stats (stats-svc, HATEOAS)
```
GET    /api/stats             → CollectionModel<EntityModel<StatResponse>>
GET    /api/stats/{category}  → EntityModel<StatResponse>
POST   /api/stats             → EntityModel<StatResponse> (201)
PUT    /api/stats/{category}  → EntityModel<StatResponse>
DELETE /api/stats/{category}  → 204
```
