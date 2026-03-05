# **Product Requirements Document (PRD): Prodee**

## **1\. Executive Summary & Vision**

**Prodee** is a full-stack, gamified "Life-OS" designed to seamlessly integrate task management, social accountability, and mental wellbeing tracking. Operating under a cozy, 8-bit RPG aesthetic, it aims to cure digital burnout by replacing clinical to-do lists with an engaging, rewarding, and deeply personalized ecosystem.

**Target Audience:** Students, young professionals, and individuals seeking structured productivity paired with emotional reflection and peer accountability.

## **2\. Core Themes & Design System**

* **Aesthetic:** "Cozy 8-bit RPG". Pixel art avatars, warm color palettes, and lo-fi audio elements.  
* **The "Twin-Linked" Philosophy:** A core belief that productivity is easier together. The platform heavily features "Duo Modes" and small Cohorts over massive, impersonal global leaderboards.  
* **Non-Intrusive Social:** Multiplayer elements rely on "Asynchronous Ghost Mode" to provide the feeling of playing alongside friends without the stress of real-time presence or the technical overhead of syncing physics.

## **3\. Functional Requirements (The "What")**

### **3.1 Domain A: The Productivity Engine**

* **Focus Islands:** Isolated workspace UI featuring a Pomodoro timer/stopwatch and ambient background audio (rain, fireplace, lo-fi beats). Sessions are logged to the database to calculate "efficiency scores."  
* **Visual Calendar:** A dynamic calendar view where users physically "cross out" days to visually represent the passage of time and approaching deadlines.  
* **Smart Task Manager:** \* CRUD operations for daily To-Dos and recurring Habits.  
  * **Intelligent Aggregator:** If a user tags a habit as "Learning" (e.g., "Read Java News"), the backend runs a daily @Scheduled task to fetch relevant articles via free public APIs (e.g., Dev.to) and displays them on the dashboard.

### **3.2 Domain B: Gamification & Economy**

* **Event-Driven XP System:** Completing tasks triggers backend events that award XP and Coins based on task difficulty.  
* **Level Progression:** Users level up at specific XP thresholds (e.g., Level 2 at 100 XP), unlocking new visual themes and avatar props.  
* **The Shop & Inventory:** Users spend Coins to purchase aesthetic upgrades, potions (for cohort battles), or tokens to unlock simple mini-games (Sudoku, Tic-Tac-Toe) designed as brief, earned brain-breaks.

### **3.3 Domain C: Social Cohorts & Accountability**

* **Cohorts & Lobbies:** Users can invite friends via unique codes or opt into an "Anonymous Matchmaking Lobby" to be paired with 4 peers for accountability.  
* **Ghost Mode Friend Battles:** An asynchronous race UI where friends' avatars progress along a track based on the tasks they check off during the day.  
* **Granular Privacy:** A strict is\_private flag on tasks ensures users can hide personal to-dos from their Cohort while still earning points.

### **3.4 Domain D: Journaling & Wellbeing**

* **Dynamic "Year in Pixels":** A highly customizable tracking grid. Users define their own log templates (e.g., Mood, Anxiety Level, Dream Quality) and assign color hex codes. A daily popup prompts the user to "paint" their pixel.  
* **Rich Text Scrapbook:** Traditional diary entries supporting text and images. Images are securely uploaded via cloud storage APIs.  
* **Manual Health/Sleep Logs:** Inputs for tracking sleep duration and digital wellbeing metrics, aggregated into weekly/monthly line graphs.

---

## **4\. Technical Architecture (The "How")**

### **4.1 Tech Stack**

* **Backend Framework:** Java 21, Spring Boot 4.0.3 (Modern enterprise standard).  
* **Database:** PostgreSQL (Relational integrity for complex user-to-cohort mapping).  
* **Security:** Spring Security with JSON Web Tokens (JWT).  
* **Real-Time Communication:** Spring WebSockets using the STOMP protocol.  
* **Data Access:** Spring Data JPA / Hibernate.  
* **External Integrations:** \* **Cloudinary API:** For stateless image hosting (Scrapbook photos, Avatars).  
  * **Dev.to / RSS APIs:** For the intelligent article aggregator.

### **4.2 System Architecture Paradigm**

Prodee will be built as a **Modular Monolith**.

Instead of a fragmented microservices nightmare or a messy layered architecture, the codebase is organized by *Feature Domain* ("Package by Feature").

* com.chhavi.prodee.productivity  
* com.chhavi.prodee.social  
* com.chhavi.prodee.gamification  
* com.chhavi.prodee.journaling

This allows for decoupled, highly cohesive code that is easy to maintain, test, and eventually split into microservices if scaling is required.

### **4.3 Database Strategy**

* **Normalization:** Utilizing Junction Tables (Cohort\_Members, User\_Inventory) to resolve Many-to-Many relationships cleanly.  
* **Entity-Attribute-Value (EAV) Alternative:** Using dynamic schema design (LogTemplate and DailyPixel tables) for the Pixel Journal, avoiding massive tables with hardcoded, unused columns.

---

## **5\. Security & Compliance Posture**

* **Stateless Authentication:** JWTs ensure the server does not need to store session states, saving memory and allowing seamless scaling.  
* **Secret Management:** Strict adherence to Twelve-Factor App methodology. All database credentials, JWT secrets, and API keys are injected exclusively via Environment Variables (application.yml). No secrets will ever be committed to version control.  
* **Role-Based Access Control (RBAC):** Cohorts utilize distinct ADMIN and MEMBER roles to manage group settings and member kicking.  
* **Data Sanitization:** All incoming DTOs (Data Transfer Objects) are validated using jakarta.validation annotations (@NotNull, @Size) before reaching the Service layer to prevent malicious injection.

---

## **6\. API Design & Documentation**

* **Global API Contract:** Every endpoint returns a standardized ApiResponse\<T\> wrapper (containing success, message, data, and timestamp) ensuring predictable parsing for the frontend.  
* **Global Exception Handling:** @ControllerAdvice intercepts all backend errors and translates them into clean, human-readable HTTP responses (e.g., intercepting a missing task and returning a 404 JSON response instead of a raw stack trace).  
* **Auto-Documentation:** Swagger UI / OpenAPI 3.0 will be integrated from Day 1 to auto-generate a live testing portal for all REST endpoints.

---

## **7\. Development Roadmap (Phased Execution)**

* **Phase 1: Foundation & Identity**  
  * Project scaffolding, CI/CD pipeline setup (GitHub Actions), PostgreSQL ERD implementation, and robust JWT User Authentication.  
* **Phase 2: The Core Loop (Productivity)**  
  * CRUD APIs for Tasks, integration of the global Exception Handler, and implementation of the "Focus Island" session logging.  
* **Phase 3: The Gamification Engine**  
  * Implementing Spring Application Events (TaskCompletedEvent) to decouple task logic from XP calculation. Building the Economy (Coins, Levels, Inventory logic).  
* **Phase 4: Social Integration**  
  * Cohort creation logic, DB junction tables, and WebSocket configuration for the real-time "Ghost Mode" progress updates.  
* **Phase 5: Journaling & Cloud Storage**  
  * Dynamic Pixel grid endpoints and integration with the Cloudinary API for secure image uploads in the Scrapbook.

