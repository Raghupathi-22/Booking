package com.hyderabad.booking.common.model

import com.hyderabad.booking.common.dto.UserDto

// Money values are Double (the backend sends JSON numbers). Dates are ISO strings such as
// "2026-11-20T10:30:00" in Asia/Kolkata wall-clock time; see DateUtils.

// ---- Auth / user ----
data class SendOtpRequest(val phone: String)
data class SendOtpResponse(val message: String? = null, val devOtp: String? = null)
data class VerifyOtpRequest(
    val phone: String,
    val otp: String,
    val name: String? = null,
    val role: String? = null
)
data class AuthResponse(val accessToken: String, val refreshToken: String? = null, val user: UserDto)
data class RefreshTokenRequest(val refreshToken: String)
data class UpdateUserRequest(val name: String? = null, val fcmToken: String? = null)

// ---- Partners & services ----
data class PartnerDto(
    val id: Long,
    val userId: Long? = null,
    val type: String? = null,            // SALON | TUTOR
    val businessName: String,
    val description: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val area: String? = null,
    val status: String? = null,          // PENDING | APPROVED | REJECTED | SUSPENDED
    val commissionPercent: Double? = null,
    val ratingAvg: Double? = null,
    val ratingCount: Int? = null
)

data class ServiceDto(
    val id: Long,
    val partnerId: Long? = null,
    val category: String? = null,        // SALON | TUITION
    val name: String,
    val description: String? = null,
    val price: Double = 0.0,
    val durationMinutes: Int = 0,
    val mode: String? = null,            // AT_SHOP | AT_HOME | ONLINE
    val active: Boolean? = true
)

data class WorkingHoursDto(
    val id: Long? = null,
    val dayOfWeek: String,
    val openTime: String,
    val closeTime: String
)

data class TimeOffDto(val id: Long? = null, val startAt: String, val endAt: String)

data class ReviewDto(
    val id: Long? = null,
    val customerId: Long? = null,
    val customerName: String? = null,
    val rating: Int = 0,
    val comment: String? = null,
    val createdAt: String? = null
)

data class PartnerDocumentDto(
    val id: Long? = null,
    val partnerId: Long? = null,
    val docType: String? = null,
    val url: String? = null
)

data class PartnerDetailDto(
    val id: Long,
    val businessName: String,
    val type: String? = null,
    val description: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val area: String? = null,
    val status: String? = null,
    val ratingAvg: Double? = null,
    val ratingCount: Int? = null,
    val services: List<ServiceDto> = emptyList(),
    val workingHours: List<WorkingHoursDto> = emptyList(),
    val timeOff: List<TimeOffDto> = emptyList(),
    val reviews: List<ReviewDto> = emptyList(),
    val documents: List<PartnerDocumentDto> = emptyList()
)

data class SlotDto(val startAt: String, val endAt: String)

// ---- Bookings & payments ----
data class BookingRequest(
    val partnerId: Long,
    val serviceId: Long,
    val startAt: String,
    val bookingType: String? = null,     // SINGLE | DEMO | RECURRING
    val recurrenceRule: String? = null   // e.g. "FREQ=WEEKLY;WEEKS=4"
)

data class BookingDto(
    val id: Long,
    val customerId: Long? = null,
    val customerName: String? = null,
    val partnerId: Long? = null,
    val businessName: String? = null,
    val serviceId: Long? = null,
    val serviceName: String? = null,
    val startAt: String,
    val endAt: String? = null,
    val status: String = "PENDING",      // PENDING | CONFIRMED | COMPLETED | CANCELLED | NO_SHOW
    val bookingType: String? = null,
    val recurrenceRule: String? = null,
    val amount: Double = 0.0,
    val commissionAmount: Double? = null,
    val paymentStatus: String? = null,   // PENDING | PAID
    val createdAt: String? = null
)

data class CreateOrderRequest(val bookingId: Long)
data class CreateOrderResponse(
    val razorpayOrderId: String,
    val amount: Double,
    val currency: String? = "INR",
    val keyId: String,
    val bookingId: Long
)
data class VerifyPaymentRequest(
    val bookingId: Long,
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String
)
data class PaymentDto(
    val id: Long? = null,
    val bookingId: Long? = null,
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null,
    val status: String? = null,
    val amount: Double? = null,
    val refundedAmount: Double? = null
)

data class ReviewRequest(val bookingId: Long, val rating: Int, val comment: String? = null)

// ---- Partner side ----
data class PartnerRegisterRequest(
    val type: String,                    // SALON | TUTOR
    val businessName: String,
    val description: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val area: String
)
data class PartnerDocumentRequest(val docType: String, val url: String)
data class ServiceRequest(
    val category: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val durationMinutes: Int,
    val mode: String,
    val active: Boolean? = true
)
data class WorkingHoursRequest(val dayOfWeek: String, val openTime: String, val closeTime: String)
data class TimeOffRequest(val startAt: String, val endAt: String)
data class PartnerEarningsDto(
    val totalBookings: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalCommission: Double = 0.0,
    val netEarnings: Double = 0.0,
    val pendingPayout: Double = 0.0
)
data class PayoutDto(
    val id: Long? = null,
    val partnerId: Long? = null,
    val amount: Double = 0.0,
    val status: String? = null,
    val periodStart: String? = null,
    val periodEnd: String? = null
)
