# Hyderabad Booking Marketplace - Phase 1

Welcome to the **Hyderabad Booking Marketplace** monorepo! This application connects customers in Hyderabad with verified service providers across **Salons** (at-shop / at-home) and **Tutors** (home tuition / online).

---

## 📁 Monorepo Structure

```text
/
├── backend/                  # Spring Boot 3 (Java 21) REST & Admin backend
├── android-common/           # Shared Kotlin library (DTOs, theme, utilities)
├── android-customer/         # Android App (Customer Jetpack Compose, MVVM)
├── android-partner/          # Android App (Partner Jetpack Compose, MVVM)
├── docs/                     # Documentation (Architecture & API Specs)
├── docker-compose.yml        # Docker composition for PostgreSQL & Backend
├── .env.example              # Environment variables template
└── README.md                 # Project instructions
```

---

## 🛠️ Prerequisites

- **Java Development Kit (JDK 17 or 21)**
- **Docker & Docker Compose** (for running PostgreSQL and Backend containerized)
- **Android Studio** (Koala / Jellyfish or newer with Kotlin 2.x and Compose support)

---

## 🚀 Getting Started

### 1. Environment Configuration
Copy `.env.example` to `.env` in the root directory (optional, default fallbacks are provided in `application.yml`):
```bash
cp .env.example .env
```

### 2. Running with Docker Compose
To start PostgreSQL and the Backend service together:
```bash
docker-compose up --build
```
- **Backend API**: `http://localhost:8080`
- **Admin Panel (Thymeleaf)**: `http://localhost:8080/admin` (Username: `admin`, Password: `admin123`)
- **Swagger OpenAPI Docs**: `http://localhost:8080/swagger-ui/index.html`

### 3. Running Backend Locally (via Gradle)
If you prefer running the Spring Boot backend locally without Docker:
```bash
cd backend
./gradlew bootRun
```
*(Ensure PostgreSQL is running locally on port 5432 with database `booking_db`, user `postgres`, password `postgres`)*

### 4. Running Android Apps
1. Open the root project folder in **Android Studio**.
2. Gradle will sync all modules (`:android-common`, `:android-customer`, `:android-partner`).
3. Select either **`android-customer`** or **`android-partner`** configuration from the run dropdown and click **Run** (`Shift + F10`) on an emulator or physical device (Min SDK 24).

---

## 🗄️ Database & Schema (`V1__initial_schema.sql`)
Flyway automatically provisions the following tables on startup:
- `users`: Customer, Partner, and Admin profiles with phone & FCM tokens.
- `partners`: Salon & Tutor business profiles, ratings, geo-coordinates, and approval status.
- `partner_documents`: Verification documents uploaded by partners.
- `services`: Service offerings (pricing, duration, mode: at-shop/at-home/online).
- `working_hours` & `time_off`: Availability schedules and leaves.
- `bookings`: Single, demo, and recurring bookings with commission tracking.
- `payments`: Razorpay integration records.
- `payouts`: Provider payout settlements.
- `reviews`: Customer ratings (1-5) and feedback.
- `notifications`: Push notification history.
- Optimized database indexes for area, rating, and time-range lookups.

## Android apps
See [docs/ANDROID.md](docs/ANDROID.md).
