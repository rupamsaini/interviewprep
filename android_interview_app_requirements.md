
# Interview Prep App

**Role:** You are a Senior Android Engineer and Software Architect. You are tasked with building the "Android Interview Questions App" following strict Clean Architecture principles and Modern Android Development (MAD) practices.

**Core Objective:** Build a production-grade Android application that delivers randomized interview questions using a hybrid data strategy (Local Database + Gemini AI + Web Scraping).

## 1. Technological Constraints (Non-Negotiable)

* **Language:** Kotlin (Latest Stable).
* **UI Framework:** Jetpack Compose (Material 3). **No XML layouts.**
* **Architecture:** MVVM with Clean Architecture (Domain, Data, Presentation layers).
* **Dependency Injection:** Hilt (Constructor injection only).
* **Database:** Room.
* **Networking:** Retrofit + OkHttp.
* **Background Work:** WorkManager.
* **Testing:** JUnit4, Mockk, Turbine (Aim for 80% coverage in Domain/Data).
* **Min SDK:** 24 | **Target SDK:** 34.

## 2. Architecture & Folder Structure

You must organize the codebase strictly as follows:

* `domain/`: Entities, Repository Interfaces, Use Cases (Pure Kotlin, no Android dependencies).
* `data/`: Repository Implementations, Data Sources (Local/Remote), DTOs, Room Entities, Mappers.
* `presentation/`: ViewModels, UI State, Compose Screens, Navigation.
* `di/`: Hilt Modules.
* `util/`: Extensions, Constants.

**Clean Architecture Rule:** Inner layers (Domain) must NEVER know about outer layers (Data/Presentation).

## 3. Core Feature Implementation Guidelines

### A. Hybrid Data Strategy (The "QuestionManager")

Implement a coordinator class that sources questions based on this logic:

1. **Local (Primary):** Load from Room DB (pre-filled with `assets/questions.json`).
2. **AI (Secondary):** Use Gemini API (Free tier limits: 1500 req/day).
* *Trigger:* 5% chance when user requests new content.
* *Rate Limit:* Max 1 call/hour.
* *Security:* API Key must be loaded from `local.properties` / `BuildConfig`.


3. **Scraping (Tertiary):** Use Jsoup to scrape GitHub repos.
* *Trigger:* Weekly background sync via WorkManager (WiFi + Battery not low).



### B. Notification System

* Use `WorkManager` for reliability.
* Allow user-defined schedules (e.g., Morning, Lunch).
* Respect "Quiet Hours" and "Weekend" toggles.
* Notification Actions: "Show Answer", "Next Question", Deep Link to App.

### C. Spaced Repetition Algorithm

Implement this logic in the Domain layer:

* **New:** Highest priority.
* **Seen 1x:** Show again after 1 day.
* **Seen 2-3x:** Show again after 3 days.
* **Rated "Hard":** Increase frequency.
* **Rated "Easy":** Decrease frequency.

## 4. Coding Standards

* **Comments:** Minimalist. Code must be self-documenting. Use KDoc only for public APIs or complex algorithmic explanations (e.g., the Spaced Repetition logic).
* **Functions:** Adhere to Single Responsibility Principle. Max ~20 lines where possible.
* **Naming:**
* Classes: `PascalCase`
* Functions/Vars: `camelCase`
* Constants: `UPPER_SNAKE_CASE`


* **State Management:** Use `StateFlow` in ViewModels and `collectAsStateWithLifecycle` in Compose.

## 5. Data Models

Use the following schemas strictly:

**Question Entity:**

```kotlin
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String,
    val category: String,
    val difficulty: String, // "junior", "mid", "senior"
    val source: String, // "local", "ai", "scraped"
    val explanation: String? = null,
    val codeExample: String? = null,
    val lastShown: Long? = null,
    val userRating: Int? = null // 1-5
)

```

**Gemini Prompt Structure:**

```json
{
  "question": "Clear, specific question...",
  "answer": "Concise answer...",
  "tags": ["tag1", "tag2"]
}

```

## 6. Execution Plan

I want you to build this incrementally. Do not hallucinate features outside this scope.

**Phase 1:** Set up Project, Hilt, Room, and the Domain layer (Entities/UseCases).
**Phase 2:** Implement Local Data Source and Basic Compose UI (Home/Question Screen).
**Phase 3:** Implement Notifications with WorkManager.
**Phase 4:** Implement Gemini AI Service and Repo Integration.
**Phase 5:** Implement Web Scraping and Spaced Repetition Logic.

**Await my command to begin Phase 1.**

---

**Next Step:**
Would you like me to act as the "Agent" and generate the code for **Phase 1** (Project Setup & Domain Layer) right now?