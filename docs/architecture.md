# Hyderabad Booking Marketplace - Architecture Overview

## Monorepo Layout
- **`/backend`**: Spring Boot 3 (Java 21) REST & Thymeleaf backend service with Spring Security (JWT), Spring Data JPA, Flyway migrations, and PostgreSQL.
- **`/android-common`**: Shared Kotlin library containing network models, theme specifications, utility functions, and shared DTOs.
- **`/android-customer`**: Jetpack Compose MVVM Android application for customers (Salons & Tutors discovery, booking flow, Razorpay integration, Room offline cache).
- **`/android-partner`**: Jetpack Compose MVVM Android application for service providers/partners (appointment management, working hours, earnings, document verification).
- **`/docs`**: Project documentation and architecture diagrams.

## Technology Stack
- **Backend**: Spring Boot 3, Spring Security, Spring Data JPA, Flyway, PostgreSQL, Thymeleaf, SpringDoc OpenAPI.
- **Android**: Kotlin, Jetpack Compose, Material Design 3 (M3), Hilt (DI), Retrofit, Room, Coroutines, Min SDK 24.
- **DevOps**: Docker & Docker Compose for containerized PostgreSQL and Backend service.
