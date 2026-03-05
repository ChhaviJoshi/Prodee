# 🎮 Prodee — The Gamified Life-OS

A full-stack **Spring Boot 4.0.3 + PostgreSQL** application that transforms daily productivity into an 8-bit RPG experience with gamification, social accountability, and journaling.

## Architecture

**Modular Monolith** organized by Feature Domain (Package-by-Feature):

```
com.chhavi.prodee
├── auth/            # JWT authentication, users, roles
├── productivity/    # Tasks, Habits, Focus Islands, Smart Feed
├── gamification/    # XP, Levels, Coins, Shop, Inventory
├── social/          # Cohorts, Leaderboards, Ghost Mode battles (WebSocket)
├── journaling/      # Year-in-Pixels, Scrapbook (Cloudinary), Health logs
└── common/          # ApiResponse wrapper, GlobalExceptionHandler, configs
```

## Tech Stack

| Layer         | Technology                          |
| ------------- | ----------------------------------- |
| Backend       | Java 21, Spring Boot 4.0.3          |
| Database      | PostgreSQL (H2 for dev)             |
| Security      | Spring Security + JWT (JJWT 0.12.6) |
| Real-Time     | Spring WebSockets + STOMP           |
| ORM           | Spring Data JPA / Hibernate         |
| Cloud Storage | Cloudinary API                      |
| Smart Feed    | Dev.to REST API                     |
| Docs          | Swagger UI / OpenAPI 3              |

## Quick Start

### Prerequisites

- Java 21+
- PostgreSQL 15+ (or use `dev` profile for in-memory H2)

### Run with H2 (no DB setup needed)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run with PostgreSQL

1. Create the database:
   ```sql
   CREATE DATABASE prodee;
   ```
2. Set environment variables:
   ```bash
   export DB_USERNAME=postgres
   export DB_PASSWORD=yourpassword
   export JWT_SECRET=yourBase64Secret
   ```
3. Run:
   ```bash
   ./mvnw spring-boot:run
   ```

### Swagger UI

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) after startup.

## API Overview

### Authentication (`/api/auth`)

| Method | Endpoint             | Description           |
| ------ | -------------------- | --------------------- |
| POST   | `/api/auth/register` | Register a new user   |
| POST   | `/api/auth/login`    | Login and receive JWT |

### Tasks (`/api/tasks`)

| Method | Endpoint                   | Description                       |
| ------ | -------------------------- | --------------------------------- |
| GET    | `/api/tasks`               | List all user tasks               |
| POST   | `/api/tasks`               | Create task                       |
| POST   | `/api/tasks/{id}/complete` | Complete task → triggers XP/Coins |

### Habits (`/api/habits`)

| Method | Endpoint                    | Description                                       |
| ------ | --------------------------- | ------------------------------------------------- |
| GET    | `/api/habits`               | List all user habits                              |
| POST   | `/api/habits`               | Create a new habit                                |
| POST   | `/api/habits/{id}/complete` | Complete habit for today → streak calc + XP/Coins |
| DELETE | `/api/habits/{id}`          | Delete a habit                                    |

### Smart Feed (`/api/articles`)

| Method | Endpoint                  | Description                                       |
| ------ | ------------------------- | ------------------------------------------------- |
| GET    | `/api/articles`           | Latest 20 aggregated articles (global)            |
| GET    | `/api/articles/my-feed`   | Personalized feed based on your habit + task tags |
| GET    | `/api/articles/tag/{tag}` | Articles filtered by a specific tag               |

### Gamification (`/api/gamification`)

| Method | Endpoint                          | Description       |
| ------ | --------------------------------- | ----------------- |
| GET    | `/api/gamification/status`        | XP, Level, Coins  |
| GET    | `/api/gamification/shop`          | Browse shop items |
| POST   | `/api/gamification/shop/buy/{id}` | Purchase item     |
| GET    | `/api/gamification/inventory`     | View inventory    |

### Cohorts (`/api/cohorts`)

| Method | Endpoint                        | Description          |
| ------ | ------------------------------- | -------------------- |
| POST   | `/api/cohorts`                  | Create cohort        |
| POST   | `/api/cohorts/join/{code}`      | Join via invite code |
| GET    | `/api/cohorts/{id}/leaderboard` | Cohort leaderboard   |

### WebSocket — Ghost Mode Battle

- **Connect:** `ws://localhost:8080/ws`
- **Send progress:** `/app/battle.progress`
- **Receive updates:** `/topic/battle/{cohortId}`

### Journaling

| Method | Endpoint                        | Description                    |
| ------ | ------------------------------- | ------------------------------ |
| POST   | `/api/journal/pixels/templates` | Create pixel template          |
| POST   | `/api/journal/pixels`           | Paint today's pixel            |
| POST   | `/api/journal/scrapbook`        | Create diary entry (multipart) |
| POST   | `/api/journal/health`           | Log sleep/mood/screen time     |

## Key Design Patterns

- **Event-Driven Architecture:** `TaskCompletedEvent` → `GamificationService` listener
- **Global `ApiResponse<T>` Wrapper:** Every endpoint returns standardized JSON
- **`@ControllerAdvice` Exception Handling:** Clean error responses, no stack traces
- **Dynamic Schema (EAV):** Pixel Journal templates are user-defined
- **RBAC:** Cohort `ADMIN/MEMBER` roles for group management
- **Stateless Auth:** JWT tokens, no server-side sessions

## Environment Variables

| Variable                | Default   | Description           |
| ----------------------- | --------- | --------------------- |
| `DB_USERNAME`           | postgres  | PostgreSQL username   |
| `DB_PASSWORD`           | postgres  | PostgreSQL password   |
| `JWT_SECRET`            | (dev key) | JWT signing secret    |
| `CLOUDINARY_CLOUD_NAME` | demo      | Cloudinary cloud name |
| `CLOUDINARY_API_KEY`    | demo      | Cloudinary API key    |
| `CLOUDINARY_API_SECRET` | demo      | Cloudinary API secret |

---

## Changelog

### v0.2.0 — Gap Fixes & Feature Additions (March 2026)

#### 1. Server Timezone Lock

- Added `@PostConstruct` in `ProdeeApplication` to set default timezone to `Asia/Kolkata`.
- Ensures all `@Scheduled` cron jobs (midnight resets, 6 AM article fetch) run at the correct IST time instead of UTC.

#### 2. Article Deduplication (Smart Feed)

- `AggregatedArticle.url` is now `unique = true` — no duplicate articles by URL.
- If the same article URL is fetched for a new tag, the new tag is **appended** to the existing article's comma-separated `tags` field (via `addTag()` helper).
- Renamed entity field from `tag` (single) → `tags` (comma-separated string).

#### 3. Article TTL Cleanup

- New `@Scheduled` job runs daily at **03:00 AM IST** (`purgeOldArticles()`).
- Bulk-deletes all `aggregated_articles` rows older than 30 days using `@Modifying` JPQL.

#### 4. Habit Streak Logic (Bulk JPQL)

- Streak is now **automatically calculated** on each completion: if yesterday was also completed → `streak++`, otherwise `streak = 1`.
- New `@Scheduled` job at **00:05 AM IST** (`resetStaleStreaks()`): runs a single `UPDATE Habit SET streak = 0 WHERE NOT EXISTS (...)` JPQL query to reset all daily habits that were missed — **no N+1 problem**.

#### 5. Habit Completion Tracking

- **New entity:** `HabitCompletion` (table: `habit_completions`) with unique constraint on `(habit_id, completed_date)` to prevent double-logging.
- **New repository:** `HabitCompletionRepository` with queries by habit, user, and date range.
- **New endpoint:** `POST /api/habits/{id}/complete` — records today's completion, updates streak, fires `HabitCompletedEvent`.
- **Gamification integration:** `GamificationService` listens for `HabitCompletedEvent` and awards **5 XP + 3 Coins** per completion, plus streak milestone bonuses:
  - 🔥 7-day streak: +20 XP, +10 Coins
  - 🔥 30-day streak: +50 XP, +30 Coins
  - 🔥 100-day streak: +100 XP, +60 Coins

#### 7. Cohort Daily Score Reset

- New `@Scheduled` job at **midnight IST** in `CohortService.resetDailyScores()`.
- Runs `UPDATE CohortMember SET dailyScore = 0` via `@Modifying` JPQL.
- The calling service method is `@Transactional` (required for `@Modifying` queries).

#### 9. Leaderboard Endpoint

- Already existed at `GET /api/cohorts/{cohortId}/leaderboard` — confirmed functional. Returns ranked members sorted by `dailyScore` descending.

#### 1 & 11. Personalized Feed + Task Tags Integration

- **New endpoint:** `GET /api/articles/my-feed` — returns articles matching the authenticated user's active habit tags **and** incomplete task tags.
- Tags are collected from:
  - `HabitRepository.findDistinctActiveTagsByUserId()` (active habits only)
  - `TaskRepository.findDistinctActiveTagsByUserId()` (incomplete tasks only, `completed = false`)
- Task tags (comma-separated in the `tags` column) are split and merged with habit tags for both the daily fetch cron and the per-user feed.
- Falls back to the global recent-20 feed if the user has no tags.

### Files Created

| File                                                     | Purpose                          |
| -------------------------------------------------------- | -------------------------------- |
| `productivity/entity/HabitCompletion.java`               | Habit completion tracking entity |
| `productivity/repository/HabitCompletionRepository.java` | Habit completion queries         |
| `gamification/event/HabitCompletedEvent.java`            | Event fired on habit completion  |

### Files Modified

| File                               | Changes                                                               |
| ---------------------------------- | --------------------------------------------------------------------- |
| `ProdeeApplication.java`           | Timezone lock (`Asia/Kolkata`)                                        |
| `AggregatedArticle.java`           | `url` unique, `tag`→`tags`, `addTag()` helper                         |
| `AggregatedArticleRepository.java` | `findByUrl()`, tag search, TTL delete                                 |
| `ArticleResponse.java`             | `tag`→`tags`                                                          |
| `ArticleAggregatorService.java`    | Dedup, TTL purge, task tag collection, personalized feed              |
| `ArticleController.java`           | `GET /my-feed` endpoint                                               |
| `TaskRepository.java`              | `findAllDistinctActiveTaskTags()`, `findDistinctActiveTagsByUserId()` |
| `HabitRepository.java`             | `findDistinctActiveTagsByUserId()`, `resetStaleStreaks()`             |
| `HabitService.java`                | `completeHabit()`, streak logic, stale streak reset cron              |
| `GamificationService.java`         | `onHabitCompleted()` listener with streak bonuses                     |
| `CohortMemberRepository.java`      | `resetAllDailyScores()`                                               |
| `CohortService.java`               | `resetDailyScores()` midnight cron                                    |
