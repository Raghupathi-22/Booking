package com.hyderabad.booking.customer

import java.io.Serializable

/** Every screen of the customer app. Serializable so the back stack survives rotation. */
sealed interface Screen : Serializable {
    data class Main(val tab: Int = 0) : Screen
    data class Results(val category: String?, val area: String, val title: String) : Screen
    data class PartnerDetail(val partnerId: Long) : Screen
    data class Book(val partnerId: Long, val serviceId: Long) : Screen
    data class Checkout(val bookingIds: ArrayList<Long>) : Screen
    data class Success(val message: String) : Screen
    data class Review(val bookingId: Long, val businessName: String) : Screen
}
