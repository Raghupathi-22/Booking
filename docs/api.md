# Hyderabad Booking Marketplace - API Specifications (Phase 1)

## Authentication Endpoints
- `POST /api/auth/login`: Authenticate via phone number / OTP / JWT.
- `POST /api/auth/register`: Register new user (Customer, Partner, Admin).

## Partner Endpoints
- `GET /api/partners`: Search partners by area, category, rating.
- `GET /api/partners/{id}`: Get partner details, working hours, and services.
- `POST /api/partners`: Register partner profile & documents.

## Booking Endpoints
- `POST /api/bookings`: Create booking (Single, Demo, Recurring).
- `GET /api/bookings/{id}`: Get booking status and payment details.
- `PUT /api/bookings/{id}/status`: Update booking status (Pending, Confirmed, Completed, Cancelled).

## Admin Panel
- `GET /admin`: Thymeleaf admin dashboard for managing users, partner verification, commissions, and platform stats. Protected by `ADMIN` role.
- Swagger UI available at `/swagger-ui/index.html`.
