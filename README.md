# Dijon Événements — API REST Backend

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=flat-square&logo=spring-boot)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=flat-square&logo=kotlin)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=jsonwebtokens)

Backend REST API for Dijon Événements — a mobile application that aggregates real cultural events from the Dijon/Burgundy region using the OpenAgenda API. Built with Spring Boot and Kotlin, containerised with Docker Compose.

---

## Demo

📱 [Download APK for Android](https://github.com/AdrianMalmierca/frontend-evenements-dijon-api/releases/latest)

**Advice:** The backend runs in Render so it takes around 60 seconds to start and after 15 minutes it sleeps again, so is normal if in the first run it takes time to log in or register.

> Enable **Unknown sources** in Settings → Security before installing. 

---

## Live Architecture

```
Android App (Kotlin)
        │
        ▼
Spring Boot API  ──────►  OpenAgenda API (events)
        │
        ▼
  PostgreSQL (users, favourites)
```

> See also: [frontend-evenements-dijon-api](https://github.com/AdrianMalmierca/frontend-evenements-dijon-api) — the native Android client for this API.

---

## Problem Statement

Cultural venues and municipalities in the Dijon area publish their events across fragmented platforms. OpenAgenda aggregates them, but their API is not designed for direct mobile consumption — it requires proxying, authentication, and personalisation (favourites, user accounts).

This backend solves that by:
- Acting as a secure proxy to OpenAgenda, exposing a clean REST API for the mobile app
- Adding user authentication with JWT so each user has their own session
- Persisting favourited events per user in PostgreSQL
- Providing a keyword search endpoint that filters events from the Dijon agenda

---

## Features

### Authentication
- Register and login with email and password
- Passwords hashed with BCrypt
- Stateless JWT authentication — no server-side sessions
- Protected endpoints via Spring Security filter chain

### Events
- Fetch real events from the Dijon Métropole OpenAgenda feed
- Keyword search across event titles and descriptions
- Events include title, description, image URL, location, GPS coordinates, dates, and categories
- Public endpoint — no authentication required to browse events

### Favourites
- Authenticated users can save and remove favourite events
- Favourites persisted in PostgreSQL with a many-to-many relationship
- Favourite list returned per user on each session

---

## Tech Stack

| Layer | Technology | Reason |
|-------|-----------|--------|
| Framework | Spring Boot 3.2.5 | Production-grade Java/Kotlin backend, standard in French ESNs |
| Language | Kotlin | Concise, null-safe, idiomatic JVM |
| Security | Spring Security + JWT (jjwt) | Industry-standard stateless auth |
| Database | PostgreSQL 16 | Reliable relational DB, widely used in production |
| ORM | Spring Data JPA + Hibernate | Type-safe queries, auto DDL |
| HTTP Client | WebFlux WebClient | Non-blocking calls to OpenAgenda API |
| Containerisation | Docker Compose | One-command local setup, production-ready |
| Build | Gradle (Kotlin DSL) | Modern build tooling for Kotlin projects |
| CI | GitHub Actions | Automated build and test on every push |

---

## Project Structure

```
dijon-events-backend/
├── src/main/kotlin/com/adrianmalmierca/dijonevents/
│   ├── DijonEventsApplication.kt       #Entry point
│   ├── client/
│   │   └── OpenAgendaClient.kt         #WebClient integration with OpenAgenda API
│   ├── config/
│   │   ├── JwtAuthFilter.kt            #JWT validation filter
│   │   ├── JwtUtil.kt                  #Token generation and validation
│   │   ├── SecurityConfig.kt           #Spring Security filter chain
│   │   └── WebClientConfig.kt          #WebClient bean configuration
│   ├── controller/
│   │   ├── AuthController.kt           #POST /api/auth/register, /api/auth/login
│   │   └── EventController.kt          #GET /api/events, /api/events/favorites
│   ├── dto/
│   │   └── Dtos.kt                     #Request/response DTOs + OpenAgenda models
│   ├── model/
│   │   ├── User.kt                     #User entity
│   │   └── FavoriteEvent.kt            #Favourite event entity
│   ├── repository/
│   │   ├── UserRepository.kt
│   │   └── FavoriteEventRepository.kt
│   └── service/
│       ├── AuthService.kt              #Register and login logic
│       ├── EventService.kt             #Event fetching and favourite management
│       └── UserDetailsServiceImpl.kt   #Spring Security UserDetailsService
├── src/main/resources/
│   └── application.yml                 #App configuration (env-based)
├── .github/workflows/
│   └── ci.yml                          #GitHub Actions — build and test
├── Dockerfile                          #Multi-stage build (Gradle → JRE Alpine)
├── docker-compose.yml                  #Backend + PostgreSQL services
├── .env.example                        #Environment variables template
└── build.gradle.kts                    #Gradle build configuration
```

---

## Running Locally

### Prerequisites
- Docker Desktop
- Java 21 (only needed if running outside Docker)

```bash
#Clone the repository
git clone https://github.com/AdrianMalmierca/evenements-dijon-api
cd evenements-dijon-api

#Set up environment variables
cp .env.example .env
#Fill in OPENAGENDA_API_KEY, OPENAGENDA_DIJON_UID, and JWT_SECRET
```

### Getting your OpenAgenda credentials

1. Register at [openagenda.com](https://openagenda.com/signin) — free account
2. Go to your profile settings and copy your **API Key**
3. Navigate to [openagenda.com/dijon-metropole](https://openagenda.com/dijon-metropole), find the agenda **UID** in the sidebar
4. Add both values to your `.env`

```bash
#Start the backend and PostgreSQL with a single command
docker compose up --build
```

The API will be available at `http://localhost:8080`.

### Environment Variables

```env
DB_USERNAME=dijon
DB_PASSWORD=dijon
JWT_SECRET=your-secret-min-32-chars   #openssl rand -base64 32
OPENAGENDA_API_KEY=your_api_key
OPENAGENDA_DIJON_UID=your_agenda_uid
```

---

## API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Create a new user account |
| POST | `/api/auth/login` | Public | Sign in and receive JWT token |

**Register / Login request body:**
```json
{
  "name": "Adrián",
  "email": "adrian@example.com",
  "password": "123456"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "adrian@example.com",
  "name": "Adrián"
}
```

### Events

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/events` | Public | List events from Dijon agenda |
| GET | `/api/events?keyword=jazz` | Public | Search events by keyword |
| GET | `/api/events/favorites` | Bearer token | Get current user's favourites |
| POST | `/api/events/favorites` | Bearer token | Add event to favourites |
| DELETE | `/api/events/favorites/{uid}` | Bearer token | Remove event from favourites |

**Event response example:**
```json
{
  "uid": "8853045",
  "title": "La Pépite #8, The 113",
  "description": "Concert, Post-punk",
  "imageUrl": "https://cdn.openagenda.com/main/f514585b4d044e77b589466addb40c63.base.image.jpg",
  "locationName": "La Vapeur",
  "address": "42 avenue de Stalingrad 21000 Dijon",
  "city": "Dijon",
  "latitude": 47.345685,
  "longitude": 5.059641,
  "dateStart": "2026-04-14T19:30:00.000+02:00",
  "dateEnd": "2026-04-14T23:30:00.000+02:00",
  "categories": ["La Vapeur", "Concert", "Dijon", "Post-punk"]
}
```

---

## Architecture Decisions

### Spring Boot over FastAPI
French ESNs and mid-sized companies predominantly use Java/Kotlin backends. Choosing Spring Boot over FastAPI or Node.js aligns with the target job market, and demonstrates familiarity with enterprise-grade frameworks.

### Kotlin over Java
Kotlin offers null safety, data classes, and extension functions that reduce boilerplate significantly while staying fully interoperable with the Java ecosystem. The `data class` pattern maps naturally to DTOs.

### WebClient as OpenAgenda Proxy
Rather than calling OpenAgenda directly from the mobile app, the backend acts as a proxy. This keeps the API key server-side, allows caching in future iterations, and decouples the mobile app from OpenAgenda's schema changes.

### Stateless JWT
Spring Security is configured as fully stateless — no `HttpSession`, no CSRF. Each request is validated independently via the `JwtAuthFilter`. This is the correct pattern for a mobile API backend.

### Multi-stage Docker Build
The `Dockerfile` uses a two-stage build: a `gradle:8.7-jdk21` image compiles the fat JAR, and an `eclipse-temurin:21-jre-alpine` image runs it. This reduces the final image size significantly and avoids shipping build tools in production.

---

## CI/CD

GitHub Actions runs on every push to `main` and `develop`:

1. Spins up a PostgreSQL service container
2. Builds the project with Gradle
3. Runs all tests

```yaml
#.github/workflows/ci.yml
on:
  push:
    branches: [main, develop]
```

---

## Future Improvements

### Short Term
- **Pagination** — add `page` and `size` parameters to the events endpoint for infinite scroll support in the app
- **Event caching** — cache OpenAgenda responses with a TTL to reduce external API calls
- **Input validation** — add `@Valid` constraints on all request bodies

### Medium Term
- **Multiple agendas** — aggregate events from several Burgundy agendas (Les Docks Numériques, Opéra de Dijon, etc.)
- **Push notifications** — notify users of upcoming favourited events
- **Filter by category** — allow filtering events by category (concert, exposition, sport...)

### Long Term
- **Deploy to Railway/Render** — live public URL for portfolio demonstrations
- **Rate limiting** — protect the OpenAgenda proxy from abuse
- **Integration tests** — Testcontainers for full end-to-end repository tests

---

## What I Learned Building This

### Spring Security Filter Chain
Configuring Spring Security for a stateless REST API requires disabling several defaults (CSRF, sessions, form login) and inserting a custom filter before `UsernamePasswordAuthenticationFilter`. Understanding the filter chain order was essential — the JWT filter must run before Spring Security evaluates authorization rules.

### Circular Bean Dependencies
Spring's default behaviour prohibits circular bean dependencies. The initial design had `AuthService` implementing `UserDetailsService`, which created a cycle with `SecurityConfig` → `JwtAuthFilter` → `AuthService` → `SecurityConfig`. The fix was extracting `UserDetailsService` into a dedicated `UserDetailsServiceImpl`, each class having a single responsibility.

### WebClient vs RestTemplate
Spring's `RestTemplate` is in maintenance mode — `WebClient` is the recommended replacement. Using it for OpenAgenda calls introduced reactive programming concepts (`Mono`, `.block()`) in a non-reactive context, which is an acceptable tradeoff for a synchronous REST controller.

### OpenAgenda API Response Mapping
OpenAgenda's event schema uses multilingual fields as maps (`{"fr": "...", "en": "..."}`) and image URLs split between a base CDN path and a filename. Mapping this correctly into clean DTOs required careful inspection of the raw API response before writing the Kotlin data classes.

---

## License

MIT — free to use, modify, and deploy.

---

## Author

**Adrián Martín Malmierca**  
Computer Engineer & Mobile Applications Master's Student  
[GitHub](https://github.com/AdrianMalmierca) · [LinkedIn](https://www.linkedin.com/in/adri%C3%A1n-mart%C3%ADn-malmierca-4aa6b0293/)

*Built as a portfolio project targeting the French tech market — ESNs and consulting firms in Burgundy/Dijon.*