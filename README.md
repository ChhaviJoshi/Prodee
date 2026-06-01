<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot 4.0.3" />
  <img src="https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT License" />
  <img src="https://img.shields.io/badge/OpenAPI-3.1-85EA2D?logo=swagger&logoColor=black" alt="OpenAPI 3.1" />
</p>

# 🎮 Prodee — The Gamified Life-OS

> Transform daily productivity into an 8-bit RPG experience — complete tasks, build streaks, earn XP & coins, shop for avatars, battle friends in real-time, and journal your journey.

Prodee is a full-stack **Spring Boot + PostgreSQL** backend that powers a gamified productivity platform. It combines task management, habit tracking, focus sessions, social cohorts, and journaling into a single, cohesive API with an RPG-style reward system.

---

## 📑 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Run with H2 (Zero Setup)](#option-1-run-with-h2-zero-setup)
  - [Run with PostgreSQL](#option-2-run-with-postgresql)
- [API Reference](#-api-reference)
  - [Authentication](#-authentication)
  - [User Profile](#-user-profile)
  - [Tasks](#-tasks)
  - [Habits](#-habits)
  - [Focus Sessions](#-focus-sessions)
  - [Gamification](#-gamification)
  - [Smart Feed](#-smart-feed)
  - [Pixel Journal](#-pixel-journal)
  - [Scrapbook](#-scrapbook)
  - [Daily Analytics](#-daily-analytics)
  - [Cohorts](#-cohorts)
  - [Countdown Calendar](#-countdown-calendar)
  - [Notifications](#-notifications)
  - [WebSocket — Ghost Mode](#-websocket--ghost-mode)
- [Environment Variables](#-environment-variables)
- [Scheduled Jobs](#-scheduled-jobs)
- [Design Patterns & Decisions](#-design-patterns--decisions)
- [Project Structure](#-project-structure)
- [Seed Data](#-seed-data)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

| Category           | Highlights                                                                         |
| ------------------ | ---------------------------------------------------------------------------------- |
| **Productivity**   | CRUD tasks with difficulty tiers (Easy → Epic), smart tagging, due dates           |
| **Habits**         | Recurring habits with automatic streak calculation and milestone bonuses            |
| **Focus Sessions** | Pomodoro-style timer logging with efficiency scoring and ambient sound selection    |
| **Gamification**   | XP + Levels + Coins economy, shop with 12 items across 5 categories, inventory     |
| **Social**         | Cohorts with invite codes, daily leaderboards, real-time Ghost Mode battles (WS)   |
| **Journaling**     | Year-in-Pixels grid, Scrapbook (Cloudinary images + sticker placement), analytics  |
| **Smart Feed**     | Aggregated Dev.to articles with personalized feed, tag filtering, deduplication     |
| **Countdown**      | Milestone tracker with visual day-grid progress                                    |
| **Notifications**  | In-app notification system with unread counts and bulk read                         |

---

## 🏗 Architecture

**Modular Monolith** organized by feature domain (package-by-feature):

```
com.chhavi.prodee
├── auth/            # JWT authentication, users, roles
├── productivity/    # Tasks, Habits, Focus Sessions, Smart Feed
├── gamification/    # XP, Levels, Coins, Shop, Inventory, Stickers
├── social/          # Cohorts, Leaderboards, Ghost Mode battles (WebSocket)
├── journaling/      # Year-in-Pixels, Scrapbook (Cloudinary), Daily Analytics
└── common/          # ApiResponse<T> wrapper, GlobalExceptionHandler, configs
```

**Request flow:**

```
Client → JWT Filter → Controller → Service → Repository → PostgreSQL
                                       ↓
                              Spring Events (async)
                                       ↓
                           GamificationService (XP/Coins)
                                       ↓
                           NotificationService (alerts)
```

---

## 🛠 Tech Stack

| Layer          | Technology                                   | Version  |
| -------------- | -------------------------------------------- | -------- |
| Language       | Java                                         | 21       |
| Framework      | Spring Boot                                  | 4.0.3    |
| Database       | PostgreSQL (H2 for dev)                      | 15+      |
| ORM            | Spring Data JPA / Hibernate                  | —        |
| Security       | Spring Security + JWT (JJWT)                 | 0.12.6   |
| Validation     | Jakarta Bean Validation                      | —        |
| Real-Time      | Spring WebSockets + STOMP                    | —        |
| Cloud Storage  | Cloudinary                                   | 1.39.0   |
| Smart Feed     | Dev.to REST API                              | —        |
| API Docs       | SpringDoc OpenAPI + Swagger UI               | 2.8.4    |
| Build Tool     | Maven Wrapper                                | —        |
| Code Gen       | Lombok                                       | —        |

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+** — [Download](https://adoptium.net/)
- **PostgreSQL 15+** — [Download](https://www.postgresql.org/download/) *(only for production profile)*
- **Maven** is NOT required — the project includes `mvnw` / `mvnw.cmd`

### Option 1: Run with H2 (Zero Setup)

No database installation needed — uses an in-memory H2 database:

```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

> H2 Console available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:prodee_dev`)

### Option 2: Run with PostgreSQL

**Step 1 — Create the database:**

```sql
CREATE DATABASE prodee;
```

**Step 2 — Set environment variables:**

```bash
# Linux / macOS
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=yourBase64SecretKeyThatIsAtLeast256BitsLong
```

```powershell
# Windows (PowerShell)
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="yourpassword"
$env:JWT_SECRET="yourBase64SecretKeyThatIsAtLeast256BitsLong"
```

**Step 3 — Run:**

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

**Step 4 — Verify:**

Open **Swagger UI** → [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 📖 API Reference

> **Base URL:** `http://localhost:8080`
> **Auth:** All endpoints except register/login require `Authorization: Bearer <jwt_token>`
> **Response format:** Every endpoint returns a standardized `ApiResponse<T>`:
> ```json
> {
>   "success": true,
>   "message": "Operation completed",
>   "data": { ... },
>   "timestamp": "2026-04-01T07:00:00"
> }
> ```

---

### 🔐 Authentication

#### `POST /api/auth/register` — Register a new user

**Request Body (JSON):**

| Field      | Type   | Required | Constraints        |
| ---------- | ------ | -------- | ------------------ |
| `username` | string | ✅       | 3–50 characters    |
| `email`    | string | ✅       | Valid email format  |
| `password` | string | ✅       | 6–100 characters   |

```json
{
  "username": "chhavi",
  "email": "chhavi@example.com",
  "password": "securePass123"
}
```

**Response:** `{ token, tokenType, userId, username }`

---

#### `POST /api/auth/login` — Login and receive JWT

**Request Body (JSON):**

| Field      | Type   | Required |
| ---------- | ------ | -------- |
| `username` | string | ✅       |
| `password` | string | ✅       |

```json
{
  "username": "chhavi",
  "password": "securePass123"
}
```

**Response:** `{ token, tokenType, userId, username }`

---

#### `GET /api/auth/profile` — Get current user profile

No request body. Uses JWT to identify the user.

**Response:** `{ id, username, email, avatarUrl, xp, level, coins, roles[], createdAt }`

---

### 👤 User Profile

| Method | Endpoint       | Description          | Required Fields |
| ------ | -------------- | -------------------- | --------------- |
| GET    | `/api/users/me` | Get current user profile | *(none — JWT)* |

---

### ✅ Tasks

| Method | Endpoint                    | Description                             |
| ------ | --------------------------- | --------------------------------------- |
| GET    | `/api/tasks`                | List all tasks for the current user     |
| GET    | `/api/tasks/{id}`           | Get a single task by ID                 |
| POST   | `/api/tasks`                | Create a new task                       |
| PUT    | `/api/tasks/{id}`           | Update an existing task                 |
| POST   | `/api/tasks/{id}/complete`  | Mark as completed → awards XP & Coins  |
| DELETE | `/api/tasks/{id}`           | Delete a task                           |

#### Create / Update Task — Request Body

| Field         | Type   | Required | Constraints                          |
| ------------- | ------ | -------- | ------------------------------------ |
| `title`       | string | ✅       | Max 200 characters                   |
| `description` | string | ❌       | Max 1000 characters                  |
| `difficulty`  | enum   | ✅       | `EASY`, `MEDIUM`, `HARD`, `EPIC`     |
| `tags`        | string | ❌       | Comma-separated (e.g. `"java,dsa"`) |
| `dueDate`     | date   | ❌       | `YYYY-MM-DD` format                  |

```json
{
  "title": "Solve 5 LeetCode problems",
  "description": "Focus on dynamic programming",
  "difficulty": "HARD",
  "tags": "dsa,java",
  "dueDate": "2026-04-07"
}
```

---

### 🔁 Habits

| Method | Endpoint                     | Description                                         |
| ------ | ---------------------------- | --------------------------------------------------- |
| GET    | `/api/habits`                | List all habits for the current user                |
| POST   | `/api/habits`                | Create a new habit                                  |
| PUT    | `/api/habits/{id}`           | Update an existing habit                            |
| POST   | `/api/habits/{id}/complete`  | Complete for today → streak calc + XP/Coins         |
| DELETE | `/api/habits/{id}`           | Delete a habit                                      |

#### Create / Update Habit — Request Body

| Field       | Type   | Required | Constraints                    |
| ----------- | ------ | -------- | ------------------------------ |
| `title`     | string | ✅       | Max 200 characters             |
| `tag`       | string | ❌       | e.g. `fitness`, `coding`       |
| `frequency` | string | ✅       | e.g. `DAILY`, `WEEKLY`         |

```json
{
  "title": "Morning run",
  "tag": "fitness",
  "frequency": "DAILY"
}
```

**Streak Milestone Bonuses (automatic):**

| Streak  | XP Bonus | Coin Bonus |
| ------- | -------- | ---------- |
| 7 days  | +20 XP   | +10 Coins  |
| 30 days | +50 XP   | +30 Coins  |
| 100 days| +100 XP  | +60 Coins  |

---

### 🎯 Focus Sessions

| Method | Endpoint                             | Description                           |
| ------ | ------------------------------------ | ------------------------------------- |
| GET    | `/api/focus-sessions`                | List all focus sessions               |
| GET    | `/api/focus-sessions/weekly`         | Past 7 days of sessions               |
| GET    | `/api/focus-sessions/productive-minutes` | Total productive minutes (integer) |
| POST   | `/api/focus-sessions`                | Log a completed focus session         |

#### Log Focus Session — Request Body

| Field                     | Type     | Required | Constraints                            |
| ------------------------- | -------- | -------- | -------------------------------------- |
| `expectedDurationMinutes` | int      | ✅       | Minimum 1                              |
| `actualDurationMinutes`   | int      | ✅       | Minimum 1                              |
| `ambientType`             | string   | ❌       | e.g. `RAIN`, `FOREST`, `CAFE`          |
| `startedAt`               | datetime | ✅       | ISO 8601 (e.g. `2026-04-01T10:00:00`)  |
| `endedAt`                 | datetime | ✅       | ISO 8601                               |

```json
{
  "expectedDurationMinutes": 25,
  "actualDurationMinutes": 23,
  "ambientType": "RAIN",
  "startedAt": "2026-04-01T10:00:00",
  "endedAt": "2026-04-01T10:23:00"
}
```

---

### 🏆 Gamification

| Method | Endpoint                                   | Description                              |
| ------ | ------------------------------------------ | ---------------------------------------- |
| GET    | `/api/gamification/status`                 | Current XP, level, coins, XP to next level |
| GET    | `/api/gamification/shop`                   | Browse all shop items                     |
| GET    | `/api/gamification/shop/available`         | Items unlocked at your current level      |
| POST   | `/api/gamification/shop/buy/{itemId}`      | Purchase a shop item (path: `itemId`)     |
| GET    | `/api/gamification/inventory`              | Your purchased items                      |
| GET    | `/api/gamification/stickers/shop`          | Browse scrapbook stickers                 |
| POST   | `/api/gamification/stickers/buy/{stickerId}` | Purchase a sticker (path: `stickerId`) |
| GET    | `/api/gamification/stickers/inventory`     | Your sticker collection                   |

**Shop Categories:** `THEME`, `AVATAR_PROP`, `POTION`, `MINI_GAME_TOKEN`, `BADGE`

> No request body needed for purchases — just pass the item/sticker ID in the path.

---

### 📰 Smart Feed

| Method | Endpoint                   | Description                                             |
| ------ | -------------------------- | ------------------------------------------------------- |
| GET    | `/api/articles`            | Latest 20 aggregated articles (global)                  |
| GET    | `/api/articles/my-feed`    | Personalized feed based on your habit + task tags       |
| GET    | `/api/articles/tag/{tag}`  | Filter articles by a specific tag (path: `tag`)         |

> Articles are fetched daily from the **Dev.to API**, deduplicated by URL, and auto-purged after 30 days.

---

### 🎨 Pixel Journal

| Method | Endpoint                                    | Description                            |
| ------ | ------------------------------------------- | -------------------------------------- |
| GET    | `/api/journal/pixels/templates`             | List all your log templates            |
| POST   | `/api/journal/pixels/templates`             | Create a custom log template           |
| PUT    | `/api/journal/pixels/templates/{templateId}` | Update a template                     |
| DELETE | `/api/journal/pixels/templates/{templateId}` | Delete a template + all its pixels    |
| POST   | `/api/journal/pixels`                       | Paint a pixel for a specific date      |
| GET    | `/api/journal/pixels/year/{year}`           | Get all pixels for a year (path: `year`) |
| GET    | `/api/journal/pixels/template/{templateId}` | Get pixels for a specific template     |

#### Create / Update Template — Request Body

| Field          | Type   | Required | Constraints                                       |
| -------------- | ------ | -------- | ------------------------------------------------- |
| `name`         | string | ✅       | Max 100 characters (e.g. `Mood`, `Energy`)        |
| `colorMapping` | string | ✅       | JSON color map (intensity → hex)                  |

```json
{
  "name": "Mood",
  "colorMapping": "{\"1\":\"#e74c3c\",\"2\":\"#e67e22\",\"3\":\"#f1c40f\",\"4\":\"#2ecc71\",\"5\":\"#27ae60\"}"
}
```

#### Paint Pixel — Request Body

| Field        | Type | Required | Constraints                    |
| ------------ | ---- | -------- | ------------------------------ |
| `templateId` | long | ✅       | ID of an existing template     |
| `date`       | date | ✅       | `YYYY-MM-DD`                   |
| `intensity`  | int  | ✅       | ≥ 1 (maps to template color)  |

```json
{
  "templateId": 1,
  "date": "2026-04-01",
  "intensity": 4
}
```

---

### 📓 Scrapbook

| Method | Endpoint                        | Description                                  |
| ------ | ------------------------------- | -------------------------------------------- |
| GET    | `/api/journal/scrapbook`        | List all scrapbook entries                   |
| GET    | `/api/journal/scrapbook/{id}`   | Get a single entry                           |
| POST   | `/api/journal/scrapbook`        | Create entry (multipart/form-data)           |
| PUT    | `/api/journal/scrapbook/{id}`   | Update entry (multipart/form-data)           |
| DELETE | `/api/journal/scrapbook/{id}`   | Delete an entry                              |

#### Create / Update Scrapbook Entry

> **Content-Type:** `multipart/form-data`

| Field            | Type   | Required | Location    | Constraints                                 |
| ---------------- | ------ | -------- | ----------- | ------------------------------------------- |
| `title`          | string | ✅       | Query param | Max 200 characters                          |
| `content`        | string | ❌       | Query param | Rich-text diary body                        |
| `placedStickers` | string | ❌       | Query param | JSON array: `[{"stickerId":1,"x":50,"y":100}]` |
| `image`          | file   | ❌       | Form body   | Image file (uploaded to Cloudinary)         |

---

### 📊 Daily Analytics

| Method | Endpoint                         | Description                                      |
| ------ | -------------------------------- | ------------------------------------------------ |
| GET    | `/api/journal/analytics`         | Get all analytics logs                           |
| GET    | `/api/journal/analytics/weekly`  | Past 7 days (optimized for charts)               |
| GET    | `/api/journal/analytics/monthly` | Past 30 days (optimized for charts)              |
| POST   | `/api/journal/analytics`         | Log daily analytics (upsert — one per user/day)  |

#### Log Daily Analytics — Request Body

| Field              | Type   | Required | Constraints |
| ------------------ | ------ | -------- | ----------- |
| `date`             | date   | ✅       | `YYYY-MM-DD` |
| `sleepHours`       | double | ✅       | ≥ 0          |
| `screenTimeHours`  | double | ✅       | ≥ 0          |
| `waterGlasses`     | int    | ❌       | ≥ 0          |
| `exerciseMinutes`  | int    | ❌       | ≥ 0          |

```json
{
  "date": "2026-04-01",
  "sleepHours": 7.5,
  "screenTimeHours": 4.2,
  "waterGlasses": 8,
  "exerciseMinutes": 30
}
```

---

### 👥 Cohorts

| Method | Endpoint                                       | Description                                |
| ------ | ---------------------------------------------- | ------------------------------------------ |
| POST   | `/api/cohorts`                                 | Create a new cohort (you become ADMIN)     |
| POST   | `/api/cohorts/join/{joinCode}`                 | Join via invite code (path: `joinCode`)    |
| GET    | `/api/cohorts/{cohortId}`                      | Get cohort details                         |
| GET    | `/api/cohorts/mine`                            | Get all cohorts you belong to              |
| GET    | `/api/cohorts/{cohortId}/leaderboard`          | Leaderboard sorted by daily score          |
| DELETE | `/api/cohorts/{cohortId}/members/{userId}`     | Kick a member (ADMIN only)                 |

#### Create Cohort — Request Body

| Field  | Type   | Required | Constraints       |
| ------ | ------ | -------- | ----------------- |
| `name` | string | ✅       | Max 100 characters |

```json
{
  "name": "Study Squad Alpha"
}
```

---

### ⏳ Countdown Calendar

| Method | Endpoint              | Description                              |
| ------ | --------------------- | ---------------------------------------- |
| GET    | `/api/milestones`     | Get all milestones with progress grids   |
| GET    | `/api/milestones/{id}` | Get a single milestone                  |
| POST   | `/api/milestones`     | Create a new milestone countdown         |
| DELETE | `/api/milestones/{id}` | Delete a milestone                      |

#### Create Milestone — Request Body

| Field        | Type   | Required | Constraints                  |
| ------------ | ------ | -------- | ---------------------------- |
| `title`      | string | ✅       | Max 200 characters           |
| `targetDate` | date   | ✅       | `YYYY-MM-DD` (future date)  |

```json
{
  "title": "Final Exams",
  "targetDate": "2026-05-15"
}
```

**Response includes:** `{ totalDays, daysPassed, daysRemaining, grid[] }` where `grid` is a boolean array representing each day.

---

### 🔔 Notifications

| Method | Endpoint                        | Description                          |
| ------ | ------------------------------- | ------------------------------------ |
| GET    | `/api/notifications`            | Get latest notifications             |
| GET    | `/api/notifications/unread-count` | Get unread notification count      |
| POST   | `/api/notifications/read-all`   | Mark all notifications as read       |

---

### 🌐 WebSocket — Ghost Mode

Real-time cohort battles using **STOMP over WebSocket**.

| Action           | Destination                    |
| ---------------- | ------------------------------ |
| **Connect**      | `ws://localhost:8080/ws`       |
| **Send progress** | `/app/battle.progress`        |
| **Subscribe**    | `/topic/battle/{cohortId}`     |

---

## ⚙ Environment Variables

| Variable                 | Default          | Description                |
| ------------------------ | ---------------- | -------------------------- |
| `DB_USERNAME`            | `postgres`       | PostgreSQL username        |
| `DB_PASSWORD`            | `postgres`       | PostgreSQL password        |
| `JWT_SECRET`             | *(dev key)*      | JWT HMAC signing secret (≥256-bit) |
| `CLOUDINARY_CLOUD_NAME`  | `demo`           | Cloudinary cloud name      |
| `CLOUDINARY_API_KEY`     | `demo`           | Cloudinary API key         |
| `CLOUDINARY_API_SECRET`  | `demo`           | Cloudinary API secret      |

> **Tip:** Copy the `.env` file and update values for your environment. For production, use a secrets manager and **never commit** real secrets.

---

## ⏰ Scheduled Jobs

| Job                    | Cron                | Description                                              |
| ---------------------- | ------------------- | -------------------------------------------------------- |
| Article Fetch          | Daily, 06:00 AM IST | Fetches articles from Dev.to for all active user tags    |
| Stale Streak Reset     | Daily, 00:05 AM IST | Resets habit streaks for users who missed yesterday       |
| Daily Score Reset      | Daily, midnight IST | Resets all `CohortMember.dailyScore` to 0                |
| Article TTL Purge      | Daily, 03:00 AM IST | Deletes articles older than 30 days                      |

> Server timezone is locked to `Asia/Kolkata` via `@PostConstruct` to ensure consistent cron execution.

---

## 🧩 Design Patterns & Decisions

| Pattern                         | Implementation                                                                                     |
| ------------------------------- | -------------------------------------------------------------------------------------------------- |
| **Event-Driven Rewards**        | `TaskCompletedEvent` / `HabitCompletedEvent` → `GamificationService` listener awards XP & Coins   |
| **Global Response Wrapper**     | `ApiResponse<T>` ensures every endpoint returns `{ success, message, data, timestamp }`            |
| **Centralized Error Handling**  | `@ControllerAdvice` with `GlobalExceptionHandler` — clean error responses, no stack traces         |
| **Dynamic Schema (EAV)**        | Pixel Journal templates are fully user-defined — name + color mapping                              |
| **RBAC**                        | Cohort-level `ADMIN` / `MEMBER` roles for group management                                        |
| **Stateless Auth**              | JWT tokens, no server-side sessions — horizontally scalable                                        |
| **Bulk JPQL Operations**        | Streak resets and score resets use single `@Modifying` JPQL queries — no N+1 problems              |
| **Upsert Semantics**            | Daily analytics uses upsert (one entry per user per day, last write wins)                          |
| **Article Deduplication**       | Unique constraint on URL + tag merging to avoid duplicates in the smart feed                       |

---

## 📂 Project Structure

```
prodee/
├── src/
│   └── main/
│       ├── java/com/chhavi/prodee/
│       │   ├── ProdeeApplication.java           # Entry point + timezone lock
│       │   ├── auth/                             # JWT, users, roles, security config
│       │   │   ├── controller/                   #   AuthController
│       │   │   ├── entity/                       #   User, Role
│       │   │   ├── repository/                   #   UserRepository, RoleRepository
│       │   │   ├── service/                      #   AuthService, JwtService
│       │   │   ├── security/                     #   JwtFilter, SecurityConfig
│       │   │   └── dto/                          #   LoginRequest, RegisterRequest, AuthResponse
│       │   ├── productivity/                     # Tasks, Habits, Focus, Articles
│       │   │   ├── controller/                   #   TaskController, HabitController, etc.
│       │   │   ├── entity/                       #   Task, Habit, HabitCompletion, etc.
│       │   │   ├── repository/                   #   TaskRepository, HabitRepository, etc.
│       │   │   ├── service/                      #   TaskService, HabitService, etc.
│       │   │   └── dto/                          #   Request/Response DTOs
│       │   ├── gamification/                     # XP, Shop, Inventory, Stickers
│       │   │   ├── controller/                   #   GamificationController
│       │   │   ├── entity/                       #   ShopItem, InventoryItem, Sticker, etc.
│       │   │   ├── event/                        #   TaskCompletedEvent, HabitCompletedEvent
│       │   │   ├── repository/                   #   ShopItemRepository, etc.
│       │   │   ├── service/                      #   GamificationService (event listener)
│       │   │   └── dto/                          #   GamificationStatus, ShopItemResponse, etc.
│       │   ├── social/                           # Cohorts, Leaderboards, WebSocket
│       │   │   ├── controller/                   #   CohortController, BattleController
│       │   │   ├── entity/                       #   Cohort, CohortMember
│       │   │   ├── repository/                   #   CohortRepository, CohortMemberRepository
│       │   │   ├── service/                      #   CohortService
│       │   │   └── dto/                          #   CohortResponse, LeaderboardEntry, etc.
│       │   ├── journaling/                       # Pixels, Scrapbook, Analytics
│       │   │   ├── controller/                   #   PixelController, ScrapbookController, etc.
│       │   │   ├── entity/                       #   LogTemplate, DailyPixel, ScrapbookEntry, etc.
│       │   │   ├── repository/
│       │   │   ├── service/
│       │   │   └── dto/
│       │   └── common/                           # Shared utilities
│       │       ├── ApiResponse.java              #   Global response wrapper
│       │       └── GlobalExceptionHandler.java   #   @ControllerAdvice
│       └── resources/
│           ├── application.yml                   # PostgreSQL config (default profile)
│           ├── application-dev.yml               # H2 config (dev profile)
│           └── data.sql                          # Seed data (roles, shop items, stickers)
├── pom.xml                                       # Maven dependencies
├── mvnw / mvnw.cmd                               # Maven Wrapper
├── .env                                          # Environment template
└── README.md
```

---

## 🌱 Seed Data

The application automatically seeds the following data on startup via `data.sql`:

**Roles:** `ROLE_USER`, `ROLE_ADMIN`

**Shop Items (12):**

| ID  | Name                 | Category         | Price | Level Required |
| --- | -------------------- | ---------------- | ----- | -------------- |
| 1   | Dark Dungeon Theme   | THEME            | 50    | 1              |
| 2   | Cozy Cottage Theme   | THEME            | 75    | 2              |
| 3   | Neon Cyberpunk Theme | THEME            | 120   | 5              |
| 4   | Pixel Knight Helmet  | AVATAR_PROP      | 30    | 1              |
| 5   | Mage Staff           | AVATAR_PROP      | 60    | 3              |
| 6   | Dragon Wings         | AVATAR_PROP      | 200   | 8              |
| 7   | Health Potion        | POTION           | 15    | 1              |
| 8   | Speed Boost Potion   | POTION           | 25    | 2              |
| 9   | Shield Potion        | POTION           | 40    | 4              |
| 10  | Sudoku Token         | MINI_GAME_TOKEN  | 10    | 1              |
| 11  | Snake Token          | MINI_GAME_TOKEN  | 10    | 1              |
| 12  | Tic-Tac-Toe Token    | MINI_GAME_TOKEN  | 10    | 1              |

**Scrapbook Stickers (9):** Star Burst, Pixel Heart, Magic Spark, Pinecone Charm, Tiny Sapling, Emerald Oak, Bonsai Grove, Budding Branch, Blossom Tree

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

Please ensure:
- Code follows the existing package-by-feature structure
- New endpoints return `ApiResponse<T>` wrappers
- Request DTOs use Jakarta validation annotations
- Any gamification triggers use Spring Events (not direct coupling)

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with ❤️ using Spring Boot & PostgreSQL
</p>
