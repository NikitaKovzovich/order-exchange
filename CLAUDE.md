# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

**OrderFlow / Order Exchange** is a microservices platform for automating order exchange between retail chains and suppliers. It has a Spring Boot (Java 21) backend in `backendOrderFlow/` and an Angular 20 frontend in `clientOrderFlow/`. The README and DEPLOYMENT docs are written in Russian.

## Repository layout

- `backendOrderFlow/` — Gradle composite build of 7 Spring Boot services (see below).
- `clientOrderFlow/` — Angular 20 SPA (standalone components, Tailwind CSS).
- `k8s/` — numbered Kubernetes manifests (`00-namespace` … `05-frontend`), applied in order.
- `sql-scripts/` — per-database init SQL (one DB per service).
- `docker-compose.yaml` — full local stack (DBs, RabbitMQ, MinIO, all services, Prometheus, Grafana).
- `deploy-docker.ps1`, `deploy-k8s-cluster.ps1` — top-level deploy orchestration.
- `monitoring/prometheus.yml` — Prometheus scrape config.
- `DEPLOYMENT.md` — full deployment guide (Russian).

## Backend architecture

The 7 services (with ports) are composed via `includeBuild` in `backendOrderFlow/settings.gradle` — **each service is its own standalone Gradle build with its own `gradlew`**, not a multi-module project:

| Service | Port | DB (MySQL) | Role |
| --- | --- | --- | --- |
| `eureka-server` | 8761 | — | Service discovery |
| `api-gateway` | 8765 | — | Spring Cloud Gateway (WebFlux), JWT auth, routing |
| `auth-service` | 8081 | `user_db` :3306 | Auth, users, companies, verification |
| `catalog-service` | 8082 | `catalog_db` :3307 | Products, catalogs, inventory |
| `order-service` | 8083 | `order_db` :3308 | Orders, cart, acceptance, analytics |
| `chat-service` | 8084 | `chat_db` :3309 | Order chat, support tickets |
| `document-service` | 8085 | `document_db` :3310 | PDF/document generation, MinIO storage |

Each service follows a layered Spring structure: `controller` → `service` → `repository` / `entity`, plus `config`, `dto`, `client`, `messaging`. (The README describes Clean Architecture/DDD aspirationally; the actual code is conventional layered Spring.)

### Authentication & gateway trust model (important)

Authentication is centralized in the **API Gateway**, and downstream services trust it via a shared secret — do not re-implement JWT parsing in services:

1. `GlobalJwtFilter` (api-gateway) validates the JWT, then injects identity headers into the proxied request: `X-User-Email`, `X-User-Role`, `X-User-Id`, `X-User-Company-Id`, plus `X-Gateway-Auth` (the shared internal secret).
2. Each downstream service has a `GatewayAuthFilter` (`config/`) that rejects any request lacking the correct `X-Gateway-Auth` header — this blocks direct access bypassing the gateway. It is gated by `gateway.auth.enabled=true` and `gateway.auth.internal-secret`.
3. Controllers read identity via `@RequestHeader("X-User-Company-Id")`, `X-User-Role`, etc. — never trust a request body for the caller's identity/company.

Relevant config keys: `JWT_SECRET`, `GATEWAY_AUTH_INTERNAL_SECRET` (env vars, defaulted for local dev in `application.properties`).

### Inter-service communication

- **Synchronous**: via RabbitMQ RPC, *not* HTTP/Feign. Services expose `@RabbitListener` queues (e.g. `OrderRpcListener` with `rpc.order.getOverallAnalytics`) and call others through `client/` classes (e.g. `AuthServiceClient`, `DocumentServiceClient`). HTTP-based service URLs in config are deprecated.
- **Asynchronous events**: `EventPublisher` writes to an `Event` table (outbox-style) and publishes to RabbitMQ exchanges like `order.events` (e.g. `OrderCreated`, `OrderConfirmed`). RabbitMQ default creds: `orderflow` / port 5672.

## Common commands

### Backend (run from inside each service dir, or use the helper scripts)

Each service uses the Gradle wrapper (`.\gradlew.bat` on Windows). Java 21 toolchain.

```powershell
# Build + test ALL services with a summary table (from backendOrderFlow/)
.\build-and-test.ps1                 # full build & test
.\build-and-test.ps1 -SkipTests      # build only
.\build-and-test.ps1 -CleanOnly      # clean only

# Single service (from e.g. backendOrderFlow/order-service/)
.\gradlew.bat build
.\gradlew.bat test                   # runs tests + jacocoTestReport
.\gradlew.bat test --tests "by.bsuir.orderservice.SomeTest"          # single test class
.\gradlew.bat test --tests "*SomeTest.someMethod"                    # single method
.\gradlew.bat bootRun                # run the service locally
```

Tests use JUnit 5 + Spring Boot Test, H2 in-memory DB, and `spring-rabbit-test`. JaCoCo coverage report is generated on `test` (verification threshold 50%, not enforced as a build gate).

### Code quality (backend)

Spotless, SpotBugs, and PMD are configured per service. **Spotless uses tab indentation** (`indentWithTabs()`) — match this in Java files.

```powershell
# From backendOrderFlow/ — aggregates across the 5 business services
.\gradlew.bat spotlessApply          # auto-format
.\gradlew.bat spotlessCheck          # verify formatting
```

SpotBugs and PMD have `ignoreFailures = true` (advisory only). `build-and-test.ps1` skips `spotbugsMain`, `spotbugsTest`, and `spotlessCheck` during its build step.

### Frontend (from clientOrderFlow/)

```powershell
npm install
npm start            # ng serve at http://localhost:4200 (proxies /api -> localhost:8765 via proxy.conf.json)
npm run build        # production build
npm test             # Karma + Jasmine
```

Prettier config lives in `package.json` (`printWidth: 100`, `singleQuote: true`, Angular HTML parser).

### Local stack & databases

```powershell
# From backendOrderFlow/
.\start-backend-with-db.ps1                  # full Docker stack (DBs, infra, monitoring, services)
.\start-backend-with-db.ps1 -LocalBackend    # only DBs/RabbitMQ/MinIO in Docker; run services from IDE
.\start-backend-with-db.ps1 -NoBuild         # start without rebuilding images
.\start-backend-with-db.ps1 -Stop            # stop everything
.\start-databases-only.ps1                   # only the MySQL containers
```

### Deployment

```powershell
.\deploy-docker.ps1 -Action up|down|restart|status|logs|build|clean
.\deploy-k8s-cluster.ps1 -Action setup|build|deploy|status|clean|all   # Minikube-based
```

### End-to-end tests

Postman/Newman collection in `backendOrderFlow/e2e/` covering ADMIN, SUPPLIER, and RETAIL_CHAIN business flows. Run via `./run-e2e.ps1` (referenced from repo root by the e2e README). See `backendOrderFlow/e2e/README.md`.

## Frontend architecture

Angular 20 standalone-component SPA. Code is organized by **role-based portals** under `src/app/`:

- `landing/`, `auth/` — public entry and login.
- `admin/` — admin portal (dashboard, analytics, users, verification, dictionaries, content, support).
- `retail-network/` — retail-chain portal (catalog, cart, orders, reception, suppliers, analytics, communications).
- `supplier/` — supplier portal (catalog, clients, orders, analytics, communications).
- Cross-cutting: `guards/` (route protection), `interceptors/` (attach JWT to requests), `services/`, `models/`.

The three portals mirror the three user roles enforced by `X-User-Role` in the backend. All API calls go through `/api` (proxied to the gateway in dev).

## Conventions

- Java package root: `by.bsuir.<service>` (e.g. `by.bsuir.orderservice`).
- Java formatting is tab-indented (enforced by Spotless) — do not convert to spaces.
- Each service owns its database exclusively; there are no cross-service DB joins — data is exchanged via RabbitMQ RPC/events only.
- Identity/authorization context comes from gateway-injected headers, never from request bodies.