package com.hyderabad.booking.common.network

import com.hyderabad.booking.common.dto.UserDto
import com.hyderabad.booking.common.model.*
import retrofit2.Call
import retrofit2.http.*

/** Every endpoint of the Spring Boot backend (/api/v1). */
interface BookingApi {

    // ---- Auth ----
    @POST("api/v1/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): AuthResponse

    /** Blocking variant used only by the OkHttp Authenticator. */
    @POST("api/v1/auth/refresh")
    fun refreshCall(@Body request: RefreshTokenRequest): Call<AuthResponse>

    // ---- Current user ----
    @GET("api/v1/me")
    suspend fun me(): UserDto

    @PUT("api/v1/me")
    suspend fun updateMe(@Body request: UpdateUserRequest): UserDto

    // ---- Discovery (public) ----
    @GET("api/v1/partners")
    suspend fun searchPartners(
        @Query("category") category: String? = null,
        @Query("area") area: String? = null,
        @Query("minRating") minRating: Double? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sort") sort: String? = null
    ): List<PartnerDto>

    @GET("api/v1/partners/{id}")
    suspend fun partnerDetail(@Path("id") id: Long): PartnerDetailDto

    @GET("api/v1/partners/{id}/slots")
    suspend fun slots(
        @Path("id") partnerId: Long,
        @Query("date") date: String,
        @Query("serviceId") serviceId: Long
    ): List<SlotDto>

    // ---- Bookings (customer or partner, depending on the logged-in user) ----
    @POST("api/v1/bookings")
    suspend fun createBooking(@Body request: BookingRequest): List<BookingDto>

    @GET("api/v1/bookings")
    suspend fun myBookings(): List<BookingDto>

    @POST("api/v1/bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Long): BookingDto

    // ---- Payments ----
    @POST("api/v1/payments/create-order")
    suspend fun createOrder(@Body request: CreateOrderRequest): CreateOrderResponse

    @POST("api/v1/payments/verify")
    suspend fun verifyPayment(@Body request: VerifyPaymentRequest): PaymentDto

    // ---- Reviews ----
    @POST("api/v1/reviews")
    suspend fun createReview(@Body request: ReviewRequest): ReviewDto

    // ---- Partner ----
    @POST("api/v1/partner/register")
    suspend fun registerPartner(@Body request: PartnerRegisterRequest): PartnerDto

    @POST("api/v1/partner/documents")
    suspend fun uploadDocument(@Body request: PartnerDocumentRequest): PartnerDocumentDto

    @GET("api/v1/partner/me")
    suspend fun partnerMe(): PartnerDto

    @GET("api/v1/partner/services")
    suspend fun partnerServices(): List<ServiceDto>

    @POST("api/v1/partner/services")
    suspend fun createService(@Body request: ServiceRequest): ServiceDto

    @PUT("api/v1/partner/services/{id}")
    suspend fun updateService(@Path("id") id: Long, @Body request: ServiceRequest): ServiceDto

    @DELETE("api/v1/partner/services/{id}")
    suspend fun deleteService(@Path("id") id: Long)

    @GET("api/v1/partner/working-hours")
    suspend fun workingHours(): List<WorkingHoursDto>

    @PUT("api/v1/partner/working-hours")
    suspend fun setWorkingHours(@Body request: List<WorkingHoursRequest>): List<WorkingHoursDto>

    @GET("api/v1/partner/time-off")
    suspend fun timeOff(): List<TimeOffDto>

    @POST("api/v1/partner/time-off")
    suspend fun addTimeOff(@Body request: TimeOffRequest): TimeOffDto

    @GET("api/v1/partner/earnings")
    suspend fun earnings(): PartnerEarningsDto

    @GET("api/v1/partner/payouts")
    suspend fun payouts(): List<PayoutDto>

    @POST("api/v1/partner/bookings/{id}/accept")
    suspend fun acceptBooking(@Path("id") id: Long): BookingDto

    @POST("api/v1/partner/bookings/{id}/reject")
    suspend fun rejectBooking(@Path("id") id: Long): BookingDto

    @POST("api/v1/partner/bookings/{id}/complete")
    suspend fun completeBooking(@Path("id") id: Long): BookingDto
}
