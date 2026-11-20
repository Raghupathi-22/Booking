package com.hyderabad.booking.partner

import java.io.Serializable

/** Screens of the partner app (after the business is approved). */
sealed interface Screen : Serializable {
    data class Main(val tab: Int = 0) : Screen
    /** serviceId = 0 means "add a new service". */
    data class ServiceEdit(val serviceId: Long = 0L) : Screen
}
