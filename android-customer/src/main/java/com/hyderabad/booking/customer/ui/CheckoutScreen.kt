package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.model.BookingDto
import com.hyderabad.booking.common.model.CreateOrderRequest
import com.hyderabad.booking.common.model.CreateOrderResponse
import com.hyderabad.booking.common.model.VerifyPaymentRequest
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.AppTopBar
import com.hyderabad.booking.common.ui.ErrorText
import com.hyderabad.booking.common.ui.LabeledRow
import com.hyderabad.booking.common.ui.LoadContent
import com.hyderabad.booking.common.util.DateUtils
import com.hyderabad.booking.common.util.Load
import com.hyderabad.booking.common.util.Money
import com.hyderabad.booking.common.util.toLoad
import com.hyderabad.booking.common.util.vmFactory
import com.hyderabad.booking.customer.BuildConfig
import com.hyderabad.booking.customer.PaymentBridge
import com.hyderabad.booking.customer.PaymentEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutState(
    val bookings: Load<List<BookingDto>> = Load.Loading,
    val busy: Boolean = false,
    val error: String? = null,
    val cancelled: Boolean = false
) {
    /** Bookings that still need a payment (recurring classes are paid one by one). */
    val unpaid: List<BookingDto>
        get() = (bookings as? Load.Ready)?.value
            ?.filter { it.paymentStatus != "PAID" && it.status != "CANCELLED" }
            .orEmpty()
}

class CheckoutViewModel(private val api: BookingApi, private val bookingIds: List<Long>) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state

    private var awaitingOrder: CreateOrderResponse? = null

    init {
        load()
        viewModelScope.launch {
            PaymentBridge.events.collect { event ->
                val order = awaitingOrder ?: return@collect // not our payment
                when (event) {
                    is PaymentEvent.Success -> verify(order, event.paymentId, event.signature)
                    is PaymentEvent.Failure -> {
                        awaitingOrder = null
                        _state.update { it.copy(busy = false, error = event.message) }
                    }
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            val r = safeApiCall { api.myBookings().filter { it.id in bookingIds } }
            _state.update { it.copy(bookings = r.toLoad()) }
        }
    }

    fun pay(startPayment: (CreateOrderResponse) -> Unit) {
        val next = _state.value.unpaid.firstOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val r = safeApiCall { api.createOrder(CreateOrderRequest(next.id)) }) {
                is ApiResult.Success -> {
                    awaitingOrder = r.data
                    startPayment(r.data) // opens Razorpay; the result arrives through PaymentBridge
                }
                is ApiResult.Error -> _state.update { it.copy(busy = false, error = r.message) }
            }
        }
    }

    /** DEBUG builds only: skips Razorpay and uses the backend's dev "test_signature". */
    fun simulatePayment() {
        val next = _state.value.unpaid.firstOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val r = safeApiCall { api.createOrder(CreateOrderRequest(next.id)) }) {
                is ApiResult.Success -> verify(r.data, "pay_dev_${System.currentTimeMillis()}", "test_signature")
                is ApiResult.Error -> _state.update { it.copy(busy = false, error = r.message) }
            }
        }
    }

    private suspend fun verify(order: CreateOrderResponse, paymentId: String, signature: String) {
        val request = VerifyPaymentRequest(order.bookingId, order.razorpayOrderId, paymentId, signature)
        when (val r = safeApiCall { api.verifyPayment(request) }) {
            is ApiResult.Success -> {
                awaitingOrder = null
                _state.update { it.copy(busy = false, error = null) }
                load()
            }
            is ApiResult.Error -> {
                awaitingOrder = null
                _state.update { it.copy(busy = false, error = r.message) }
            }
        }
    }

    /** Frees the slots of bookings that were never paid. */
    fun cancelUnpaid() {
        val toCancel = _state.value.unpaid
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            toCancel.forEach { safeApiCall { api.cancelBooking(it.id) } }
            _state.update { it.copy(busy = false, cancelled = true) }
        }
    }
}

@Composable
fun CheckoutScreen(
    api: BookingApi,
    bookingIds: List<Long>,
    startPayment: (CreateOrderResponse) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onCancelled: () -> Unit
) {
    val vm: CheckoutViewModel = viewModel(
        key = "checkout_${bookingIds.joinToString("_")}",
        factory = vmFactory { CheckoutViewModel(api, bookingIds) }
    )
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.cancelled) { if (state.cancelled) onCancelled() }
    LaunchedEffect(state.bookings, state.busy) {
        val loaded = state.bookings as? Load.Ready
        if (loaded != null && loaded.value.isNotEmpty() && state.unpaid.isEmpty() && !state.busy &&
            loaded.value.any { it.paymentStatus == "PAID" }
        ) onDone()
    }

    Scaffold(topBar = { AppTopBar("Payment", onBack) }) { padding ->
        LoadContent(state.bookings, onRetry = vm::load) { bookings ->
            val unpaid = state.unpaid
            val next = unpaid.firstOrNull()
            Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                if (bookings.isNotEmpty()) {
                    val first = bookings.first()
                    Text(first.businessName ?: "", style = MaterialTheme.typography.titleLarge)
                    Text(first.serviceName ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                }
                bookings.forEachIndexed { i, b ->
                    LabeledRow(
                        if (bookings.size > 1) "Class ${i + 1} • ${DateUtils.dateTime(b.startAt)}" else DateUtils.dateTime(b.startAt),
                        if (b.paymentStatus == "PAID") "Paid" else Money.rupees(b.amount)
                    )
                }
                Spacer(Modifier.weight(1f))

                ErrorText(state.error)
                Spacer(Modifier.height(8.dp))
                if (next != null) {
                    Button(
                        onClick = { vm.pay(startPayment) },
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (state.busy) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            val suffix = if (unpaid.size > 1) " (${bookings.size - unpaid.size + 1} of ${bookings.size})" else ""
                            Text("Pay ${Money.rupees(next.amount)}$suffix")
                        }
                    }
                    if (BuildConfig.DEBUG) {
                        OutlinedButton(
                            onClick = vm::simulatePayment,
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("DEV: simulate successful payment") }
                    }
                    TextButton(onClick = vm::cancelUnpaid, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel this booking")
                    }
                }
            }
        }
    }
}
