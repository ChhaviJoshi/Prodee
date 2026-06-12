<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot 4.0.3" />
  <img src="https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/React-19.2.4-61DAFB?logo=react&logoColor=black" alt="React 19" />
  <img src="https://img.shields.io/badge/Vite-7.3.1-646CFF?logo=vite&logoColor=white" alt="Vite" />
</p>

# 🎮 Prodee — The Gamified Life-OS

> Transform daily productivity into an 8-bit RPG experience — complete tasks, build streaks, earn XP & coins, shop for avatars, battle friends in real-time, and journal your journey.

Prodee is a full-stack web application featuring a **Spring Boot + PostgreSQL** backend and a **React + Vite** frontend. It combines task management, habit tracking, focus sessions, social cohorts, and journaling into a single, cohesive platform with an RPG-style reward system.

---

## ✨ Features

- **Productivity & Habits**: CRUD tasks with difficulty tiers, smart tagging, due dates, and recurring habits with streak tracking.
- **Focus Sessions**: Pomodoro-style timer logging with efficiency scoring.
- **Gamification**: Earn XP, levels, and coins. Shop for themes, avatars, and potions.
- **Social**: Cohorts with invite codes, daily leaderboards, and real-time Ghost Mode battles.
- **Journaling**: Year-in-Pixels grid, Scrapbook, and daily analytics.
- **Smart Feed**: Aggregated Dev.to articles based on personalized tags.

---

## 🏗 Tech Stack

- **Backend**: Java 21, Spring Boot 4.0.3, PostgreSQL (H2 for dev), Spring Security + JWT, WebSockets (STOMP)
- **Frontend**: React 19, React Router, Tailwind CSS 4, Vite
- **Cloud Storage**: Cloudinary (for scrapbook images)

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+**
- **Node.js 20+** and npm
- **PostgreSQL 15+** (optional for dev, can use H2)

### 1. Running the Backend

The backend can be run with an in-memory H2 database (zero setup) or with PostgreSQL.

**Option A: Run with H2 (Zero Setup)**

```bash
# Navigate to project root
cd Prodee

# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

> H2 Console: `http://localhost:8080/h2-console`
> Swagger API Docs: `http://localhost:8080/swagger-ui.html`

**Option B: Run with PostgreSQL**

Create a database named `prodee` and set the following environment variables (or update `.env`):
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

```bash
# Run backend
# Linux / macOS
./mvnw spring-boot:run

# Windows (PowerShell)

cd C:\Projects\Prodee
Get-Content .env | ForEach-Object { if ($_ -match '^\s*([^#][^=]+)=(.*)$') { [System.Environment]::SetEnvironmentVariable($Matches[1].Trim(), $Matches[2].Trim(), 'Process') } }
.\mvnw.cmd spring-boot:run
.\mvnw.cmd spring-boot:run
```

### 2. Running the Frontend

The frontend is a Vite + React application located in the `prodee-ui` directory.

```bash
# Navigate to the frontend directory
cd Prodee/prodee-ui

# Install dependencies
npm install

# Start the development server
npm run dev
```

> The frontend will typically be available at `http://localhost:5173`

---

## ⚙ Environment Variables

Copy the `.env` file in the root directory and update values for your environment if needed.

**Backend (`.env`)**
```env
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=yourBase64SecretKeyThatIsAtLeast256BitsLong
CLOUDINARY_CLOUD_NAME=demo
CLOUDINARY_API_KEY=demo
CLOUDINARY_API_SECRET=demo
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.
