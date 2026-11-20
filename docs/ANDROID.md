# Android apps - setup and notes

Modules: `android-common` (API client, models, login, theme) + `android-customer` + `android-partner`.
Backend: `backend/` (Spring Boot). Admin panel: served by the backend at `/admin`.

## Run it
1. Start the backend: `docker-compose up` (or run `backend` from your IDE). API: http://localhost:8080
2. Open this folder in Android Studio, let Gradle sync, run `android-customer` or `android-partner`.
3. Emulator: the apps call `http://10.0.2.2:8080/`. Real phone: build with
   `-PbookingBaseUrl=http://<your-PC-WiFi-IP>:8080/` (phone and PC on the same Wi-Fi).
4. Login uses phone OTP. The dev backend returns the OTP in the response and the app shows it as "Dev OTP".

## Test flow
1. Partner app: log in with phone A -> register business -> (admin) approve at http://localhost:8080/admin
   -> add a service -> set working hours.
2. Customer app: log in with phone B -> search -> book a slot -> Checkout.
   In DEBUG builds use "DEV: simulate successful payment" (backend accepts `test_signature`).
3. Partner app: booking appears under Upcoming -> "Mark done" after it starts.
4. Customer app: Bookings -> Past -> Rate.

## Payments (Razorpay)
- Put real test keys in `.env` (RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET) to use the real checkout screen.
- **Production:** set `RAZORPAY_ALLOW_TEST_SIGNATURE=false`, otherwise anyone can mark a booking as paid.

## Backend endpoints added for the apps
- `POST /api/v1/partner/bookings/{id}/accept | reject | complete`
- `POST /api/v1/reviews`

## Known gaps (not built yet)
- Push notifications (needs a Firebase project + google-services.json)
- Image upload (partner documents are links for now)
- Offline cache (Room is wired in but unused)
- Unpaid PENDING bookings never expire on the server (the app lets users cancel them)
- One phone number = one role (customer OR partner)
