# 🎮 Prodee — Complete API Reference

> Extracted from the live OpenAPI 3.1 spec (`/v3/api-docs`). All authenticated endpoints require a **Bearer JWT token** in the `Authorization` header.

---

## 1. 🔐 Authentication

### `POST /api/auth/register` — Register a new user

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `username` | string | ✅ | 3–50 characters |
| `email` | string | ✅ | Valid email |
| `password` | string | ✅ | 6–100 characters |

**Returns:** `{ token, tokenType, userId, username }`

---

### `POST /api/auth/login` — Login and receive JWT token

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `username` | string | ✅ | — |
| `password` | string | ✅ | — |

**Returns:** `{ token, tokenType, userId, username }`

---

### `GET /api/auth/profile` — Get current user profile

| Field | Type | Required |
|-------|------|----------|
| *(none — uses JWT)* | — | — |

**Returns:** `{ id, username, email, avatarUrl, xp, level, coins, roles[], createdAt }`

---

## 2. 👤 Users

### `GET /api/users/me` — Get current user profile

| Field | Type | Required |
|-------|------|----------|
| *(none — uses JWT)* | — | — |

**Returns:** `{ id, username, email, avatarUrl, xp, level, coins, roles[], createdAt }`

---

## 3. ✅ Tasks

### `POST /api/tasks` — Create a new task

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `title` | string | ✅ | 0–200 characters |
| `description` | string | ❌ | 0–1000 characters |
| `difficulty` | enum | ✅ | `EASY`, `MEDIUM`, `HARD`, `EPIC` |
| `tags` | string | ❌ | Comma-separated tags |
| `dueDate` | date | ❌ | `YYYY-MM-DD` format |

**Returns:** `{ id, title, description, difficulty, completed, tags, dueDate, completedAt, createdAt }`

---

### `GET /api/tasks` — Get all tasks for the current user

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/tasks/{id}` — Get a task by ID

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

### `PUT /api/tasks/{id}` — Update a task

| Field | Type | Required | Location / Constraints |
|-------|------|----------|------------------------|
| `id` | long | ✅ | Path |
| `title` | string | ✅ | Body — 0–200 chars |
| `description` | string | ❌ | Body — 0–1000 chars |
| `difficulty` | enum | ✅ | Body — `EASY` / `MEDIUM` / `HARD` / `EPIC` |
| `tags` | string | ❌ | Body — comma-separated |
| `dueDate` | date | ❌ | Body — `YYYY-MM-DD` |

---

### `POST /api/tasks/{id}/complete` — Mark task as completed → triggers XP/Coin rewards

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

### `DELETE /api/tasks/{id}` — Delete a task

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

## 4. 🔁 Habits

### `POST /api/habits` — Create a new habit

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `title` | string | ✅ | 0–200 characters |
| `tag` | string | ❌ | e.g. `fitness`, `coding` |
| `frequency` | string | ✅ | e.g. `DAILY`, `WEEKLY` |

**Returns:** `{ id, title, tag, frequency, streak, active, createdAt }`

---

### `GET /api/habits` — Get all habits for the current user

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `PUT /api/habits/{id}` — Update a habit

| Field | Type | Required | Location / Constraints |
|-------|------|----------|------------------------|
| `id` | long | ✅ | Path |
| `title` | string | ✅ | Body — 0–200 chars |
| `tag` | string | ❌ | Body |
| `frequency` | string | ✅ | Body |

---

### `POST /api/habits/{id}/complete` — Complete a habit for today (updates streak, awards XP)

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

### `POST /api/habits/{id}/streak` — Increment streak *(deprecated — use `/complete`)*

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

### `DELETE /api/habits/{id}` — Delete a habit

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

## 5. 🎯 Focus Sessions

### `POST /api/focus-sessions` — Log a completed focus session

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `expectedDurationMinutes` | int | ✅ | Minimum 1 |
| `actualDurationMinutes` | int | ✅ | Minimum 1 |
| `ambientType` | string | ❌ | e.g. `RAIN`, `FOREST`, `CAFE` |
| `startedAt` | datetime | ✅ | ISO 8601 (`2026-04-01T10:00:00`) |
| `endedAt` | datetime | ✅ | ISO 8601 |

**Returns:** `{ id, expectedDurationMinutes, actualDurationMinutes, efficiencyScore, ambientType, startedAt, endedAt }`

---

### `GET /api/focus-sessions` — Get all focus sessions

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/focus-sessions/weekly` — Get focus sessions from the past 7 days

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/focus-sessions/productive-minutes` — Get total productive minutes

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

**Returns:** `integer` (total minutes)

---

## 6. 🏆 Gamification

### `GET /api/gamification/status` — Get current XP, level, coins

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

**Returns:** `{ userId, username, xp, level, coins, xpToNextLevel }`

---

### `GET /api/gamification/shop` — Get all shop items

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

**Returns:** List of `{ id, name, description, category, price, imageUrl, levelRequired }`
- **Categories:** `THEME`, `AVATAR_PROP`, `POTION`, `MINI_GAME_TOKEN`, `BADGE`

---

### `GET /api/gamification/shop/available` — Get shop items available at your current level

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `POST /api/gamification/shop/buy/{itemId}` — Purchase an item from the shop

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `itemId` | long | ✅ | Path |

**Returns:** `{ inventoryId, itemId, itemName, category, quantity, acquiredAt }`

---

### `GET /api/gamification/inventory` — Get your inventory

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/gamification/stickers/shop` — Get all available scrapbook stickers

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

**Returns:** List of `{ id, name, imageUrl, price }`

---

### `POST /api/gamification/stickers/buy/{stickerId}` — Purchase a scrapbook sticker

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `stickerId` | long | ✅ | Path |

**Returns:** `{ inventoryId, stickerId, stickerName, imageUrl, quantity, acquiredAt }`

---

### `GET /api/gamification/stickers/inventory` — Get your sticker inventory

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

## 7. 📰 Smart Feed (Articles)

### `GET /api/articles` — Get recent aggregated articles (global)

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

**Returns:** List of `{ id, title, url, source, tags, coverImageUrl, fetchedAt }`

---

### `GET /api/articles/my-feed` — Get personalized articles based on your habit/task tags

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/articles/tag/{tag}` — Get articles filtered by a specific tag

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `tag` | string | ✅ | Path |

---

## 8. 🎨 Pixel Journal

### `POST /api/journal/pixels/templates` — Create a custom log template

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | string | ✅ | 0–100 characters (e.g. `Mood`, `Energy`) |
| `colorMapping` | string | ✅ | JSON-encoded map — e.g. `{"1":"#ff0000","2":"#00ff00","3":"#0000ff"}` |

**Returns:** `{ id, name, colorMapping, createdAt }`

---

### `PUT /api/journal/pixels/templates/{templateId}` — Update an existing template

| Field | Type | Required | Location / Constraints |
|-------|------|----------|------------------------|
| `templateId` | long | ✅ | Path |
| `name` | string | ✅ | Body — 0–100 chars |
| `colorMapping` | string | ✅ | Body — JSON color map |

---

### `DELETE /api/journal/pixels/templates/{templateId}` — Delete a template and all its pixels

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `templateId` | long | ✅ | Path |

---

### `GET /api/journal/pixels/templates` — Get all your log templates

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `POST /api/journal/pixels` — Paint a pixel for a specific date

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `templateId` | long | ✅ | ID of an existing template |
| `date` | date | ✅ | `YYYY-MM-DD` format |
| `intensity` | int | ✅ | Minimum 1 (maps to color in template) |

**Returns:** `{ id, templateName, date, intensity, colorHex }`

---

### `GET /api/journal/pixels/year/{year}` — Get all pixels for a year

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `year` | int | ✅ | Path (e.g. `2026`) |

---

### `GET /api/journal/pixels/template/{templateId}` — Get all pixels for a specific template

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `templateId` | long | ✅ | Path |

---

## 9. 📓 Scrapbook

### `POST /api/journal/scrapbook` — Create a scrapbook entry (multipart)

| Field | Type | Required | Location | Constraints |
|-------|------|----------|----------|-------------|
| `title` | string | ✅ | Query param | 0–200 characters |
| `content` | string | ❌ | Query param | Rich-text body |
| `placedStickers` | string | ❌ | Query param | JSON array of `[{stickerId, x, y}]` |
| `image` | file | ❌ | Multipart body | Image file upload (Cloudinary) |

**Returns:** `{ id, title, content, imageUrl, placedStickers[], createdAt, updatedAt }`

---

### `PUT /api/journal/scrapbook/{id}` — Update a scrapbook entry

| Field | Type | Required | Location | Constraints |
|-------|------|----------|----------|-------------|
| `id` | long | ✅ | Path | — |
| `title` | string | ✅ | Query param | 0–200 chars |
| `content` | string | ❌ | Query param | — |
| `placedStickers` | string | ❌ | Query param | JSON: `[{stickerId, x, y}]` |
| `image` | file | ❌ | Multipart body | New image |

---

### `GET /api/journal/scrapbook` — Get all scrapbook entries

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/journal/scrapbook/{id}` — Get a single scrapbook entry

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

### `DELETE /api/journal/scrapbook/{id}` — Delete a scrapbook entry

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

## 10. 📊 Daily Analytics

### `POST /api/journal/analytics` — Log daily analytics (upsert — one entry per day)

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `date` | date | ✅ | `YYYY-MM-DD` |
| `sleepHours` | double | ✅ | Minimum 0 |
| `screenTimeHours` | double | ✅ | Minimum 0 |
| `waterGlasses` | int | ❌ | Minimum 0 |
| `exerciseMinutes` | int | ❌ | Minimum 0 |

**Returns:** `{ id, date, sleepHours, screenTimeHours, waterGlasses, exerciseMinutes }`

---

### `GET /api/journal/analytics` — Get all analytics logs

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/journal/analytics/weekly` — Past 7 days (optimized for charts)

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/journal/analytics/monthly` — Past 30 days (optimized for charts)

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

## 11. 👥 Cohorts

### `POST /api/cohorts` — Create a new cohort (you become ADMIN)

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | string | ✅ | 0–100 characters |

**Returns:** `{ id, name, joinCode, members[], createdAt }`

---

### `POST /api/cohorts/join/{joinCode}` — Join a cohort using its invite code

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `joinCode` | string | ✅ | Path |

---

### `GET /api/cohorts/{cohortId}` — Get cohort details

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `cohortId` | long | ✅ | Path |

---

### `GET /api/cohorts/mine` — Get all cohorts you belong to

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/cohorts/{cohortId}/leaderboard` — Get cohort leaderboard by daily score

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `cohortId` | long | ✅ | Path |

**Returns:** List of `{ rank, userId, username, dailyScore, weeklyScore, firstPlaceFinishes, level }`

---

### `DELETE /api/cohorts/{cohortId}/members/{userId}` — Kick a member (ADMIN only)

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `cohortId` | long | ✅ | Path |
| `userId` | long | ✅ | Path |

---

## 12. ⏳ Countdown Calendar (Milestones)

### `POST /api/milestones` — Create a new milestone countdown

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `title` | string | ✅ | 0–200 characters |
| `targetDate` | date | ✅ | `YYYY-MM-DD` (future date) |

**Returns:** `{ id, title, startDate, targetDate, totalDays, daysPassed, daysRemaining, grid[] }`

---

### `GET /api/milestones` — Get all milestones with progress grids

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

### `GET /api/milestones/{id}` — Get a single milestone

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

### `DELETE /api/milestones/{id}` — Delete a milestone

| Field | Type | Required | Location |
|-------|------|----------|----------|
| `id` | long | ✅ | Path |

---

## 13. 🔔 Notifications

### `GET /api/notifications` — Get latest notifications

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

**Returns:** List of `{ id, type, message, read, createdAt }`

---

### `GET /api/notifications/unread-count` — Get unread notification count

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

**Returns:** `{ count: <number> }`

---

### `POST /api/notifications/read-all` — Mark all notifications as read

| Field | Type | Required |
|-------|------|----------|
| *(none)* | — | — |

---

## 14. 🌐 WebSocket — Ghost Mode Battle

| Item | Value |
|------|-------|
| **Connect** | `ws://localhost:8080/ws` |
| **Send progress** | `/app/battle.progress` |
| **Receive updates** | `/topic/battle/{cohortId}` |

> [!NOTE]
> WebSocket endpoints use STOMP protocol over SockJS. Not documented in OpenAPI — requires a WebSocket client.
