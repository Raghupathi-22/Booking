package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.model.BookingDto
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.EmptyBox
import com.hyderabad.booking.common.ui.ErrorText
import com.hyderabad.booking.common.ui.LoadContent
import com.hyderabad.booking.common.ui.StatusChip
import com.hyderabad.booking.common.util.DateUtils
import com.hyderabad.booking.common.util.Load
import com.hyderabad.booking.common.util.Money
import com.hyderabad.booking.common.util.toLoad
import com.hyderabad.booking.common.util.vmFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingsState(
    val bookings: Load<List<BookingDto>> = Load.Loading,
    val error: String? = null
)

class BookingsViewModel(private val api: BookingApi) : ViewModel() {
    private val _state = MutableStateFlow(BookingsState())
    val state: StateFlow<BookingsState> = _state

    fun refresh() {
        viewModelScope.launch {
            val r = safeApiCall { api.myBookings() }
            // Keep showing the old list while refreshing; only show the loader the first time.
            _state.update { it.copy(bookings = r.toLoad()) }
        }
    }

    fun cancel(id: Long) {
        viewModelScope.launch {
            when (val r = safeApiCall { api.cancelBooking(id) }) {
                is ApiResult.Success -> { _state.update { it.copy(error = null) }; refresh() }
                is ApiResult.Error -> _state.update { it.copy(error = r.message) }
            }
        }
    }
}

@Composable
fun BookingsScreen(
    api: BookingApi,
    modifier: Modifier = Modifier,
    onPay: (Long) -> Unit,
    onReview: (bookingId: Long, businessName: String) -> Unit
) {
    val vm: BookingsViewModel = viewModel(factory = vmFactory { BookingsViewModel(api) })
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var toCancel by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Text("My bookings", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        PrimaryTabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Upcoming") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Past") })
        }
        ErrorText(state.error, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

        LoadContent(state.bookings, onRetry = vm::refresh) { all ->
            val upcoming = all
                .filter { it.status in listOf("PENDING", "CONFIRMED") && DateUtils.isFuture(it.endAt ?: it.startAt) }
                .sortedBy { it.startAt }
            val past = all.filter { it !in upcoming }.sortedByDescending { it.startAt }
            val list = if (tab == 0) upcoming else past

            if (list.isEmpty()) {
                EmptyBox(if (tab == 0) "No upcoming bookings.\nFind a salon or tutor on the Home tab." else "No past bookings yet.")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { b ->
                        BookingCard(
                            booking = b,
                            canCancel = tab == 0,
                            onCancel = { toCancel = b.id },
                            onPay = { onPay(b.id) },
                            onReview = { onReview(b.id, b.businessName ?: "") }
                        )
                    }
                }
            }
        }
    }

    toCancel?.let { id ->
        AlertDialog(
            onDismissRequest = { toCancel = null },
            title = { Text("Cancel booking?") },
            text = { Text("Free cancellation is available up to 2 hours before the start time. Refunds for paid bookings are handled by support.") },
            confirmButton = { TextButton(onClick = { vm.cancel(id); toCancel = null }) { Text("Yes, cancel") } },
            dismissButton = { TextButton(onClick = { toCancel = null }) { Text("Keep it") } }
        )
    }
}

@Composable
private fun BookingCard(
    booking: BookingDto,
    canCancel: Boolean,
    onCancel: () -> Unit,
    onPay: () -> Unit,
    onReview: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(booking.businessName ?: "Booking", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusChip(booking.status)
            }
            Text(booking.serviceName ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(DateUtils.dateTime(booking.startAt), style = MaterialTheme.typography.bodyLarge)
            Text(
                if (booking.amount == 0.0) "Free" else "${Money.rupees(booking.amount)} • ${booking.paymentStatus?.lowercase() ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val needsPayment = booking.status == "PENDING" && booking.paymentStatus != "PAID" && booking.amount > 0
            val canReview = booking.status == "COMPLETED"
            val canCancelNow = canCancel && booking.status in listOf("PENDING", "CONFIRMED")
            if (needsPayment || canReview || canCancelNow) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (needsPayment) Button(onClick = onPay) { Text("Pay now") }
                    if (canReview) Button(onClick = onReview) { Text("Rate") }
                    if (canCancelNow) OutlinedButton(onClick = onCancel) { Text("Cancel") }
                }
            }
        }
    }
}
