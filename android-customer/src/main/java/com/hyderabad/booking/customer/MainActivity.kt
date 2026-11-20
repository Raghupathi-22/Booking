package com.hyderabad.booking.customer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.hyderabad.booking.common.AppContainer
import com.hyderabad.booking.common.model.CreateOrderResponse
import com.hyderabad.booking.common.ui.AppFlavor
import com.hyderabad.booking.common.ui.BookingTheme
import com.hyderabad.booking.customer.ui.CustomerRoot
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = (application as CustomerApp).container
        Checkout.preload(applicationContext)

        setContent {
            BookingTheme(AppFlavor.CUSTOMER) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CustomerRoot(container = container, startPayment = ::openRazorpay)
                }
            }
        }
    }

    private fun openRazorpay(order: CreateOrderResponse) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(order.keyId)
            val user = container.tokenStore.user
            val options = JSONObject().apply {
                put("name", "Hyderabad Booking")
                put("description", "Booking #${order.bookingId}")
                put("order_id", order.razorpayOrderId)
                put("currency", order.currency ?: "INR")
                put("amount", Math.round(order.amount * 100)) // paise
                put("prefill", JSONObject().apply {
                    put("contact", user?.phone ?: "")
                })
            }
            checkout.open(this, options)
        } catch (e: Exception) {
            PaymentBridge.events.tryEmit(PaymentEvent.Failure(e.message ?: "Could not start payment"))
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, data: PaymentData?) {
        val paymentId = razorpayPaymentId ?: data?.paymentId
        val orderId = data?.orderId
        val signature = data?.signature
        if (paymentId == null || orderId == null || signature == null) {
            PaymentBridge.events.tryEmit(PaymentEvent.Failure("Payment completed but could not be verified. Contact support."))
        } else {
            PaymentBridge.events.tryEmit(PaymentEvent.Success(paymentId, orderId, signature))
        }
    }

    override fun onPaymentError(code: Int, response: String?, data: PaymentData?) {
        Toast.makeText(this, "Payment was not completed", Toast.LENGTH_SHORT).show()
        PaymentBridge.events.tryEmit(PaymentEvent.Failure("Payment was not completed. You can try again."))
    }
}
