package com.hyderabad.booking.customer

import kotlinx.coroutines.flow.MutableSharedFlow

sealed interface PaymentEvent {
    data class Success(val paymentId: String, val orderId: String, val signature: String) : PaymentEvent
    data class Failure(val message: String) : PaymentEvent
}

/** The Razorpay SDK reports results to the Activity; this hands them to the ViewModels. */
object PaymentBridge {
    val events = MutableSharedFlow<PaymentEvent>(extraBufferCapacity = 4)
}
