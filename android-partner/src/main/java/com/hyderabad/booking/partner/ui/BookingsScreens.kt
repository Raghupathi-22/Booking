package com.hyderabad.booking.partner.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.hyderabad.booking.common.model.PartnerEarningsDto
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

data class PartnerBookingsState(
    val bookings: Load<List<BookingDto>> = Load.Loading,
    val earnings: PartnerEarningsDto? = null,
    val error: String? = null,
    val busyId: Long? = null
)

class PartnerBookingsViewModel(private val api: BookingApi) : ViewModel() {
    private val _state = MutableStateFlow(PartnerBookingsState())
    val state: StateFlow<PartnerBookingsState> = _state

    fun refresh() {
        viewModelScope.launch {
            val b = safeApiCall { api.myBookings() }.toLoad()
            val e = (safeApiCall { api.earnings() } as? ApiResult.Success)?.data
            _state.update { it.copy(bookings = b, earnings = e ?: it.earnings) }
        }
    }

    fun act(id: Long, action: String) {
        viewModelScope.launch {
            _state.update { it.copy(busyId = id, error = null) }
            val r = safeApiCall {
                when (action) {
                    "accept" -> api.acceptBooking(id)
                    "reject" -> api.rejectBooking(id)
                    else -> api.completeBooking(id)
                }
            }
            when (r) {
                is ApiResult.Success -> { _state.update { it.copy(busyId = null) }; refresh() }
                is ApiResult.Error -> _state.update { it.copy(busyId = null, error = r.message) }
            }
        }
    }
}

@Composable
fun DashboardScreen(api: BookingApi, modifier: Modifier = Modifier, onSeeBookings: () -> Unit) {
    val vm: PartnerBookingsViewModel = viewModel(factory = vmFactory { PartnerBookingsViewModel(api) })
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        LoadContent(state.bookings, onRetry = vm::refresh) { all ->
            val todayPrefix = DateUtils.nextDays(1).first()
            val today = all.filter { it.startAt.startsWith(todayPrefix) && it.status in listOf("PENDING", "CONFIRMED") }
                .sortedBy { it.startAt }
            val needsAction = all.filter { it.status == "PENDING" && it.paymentStatus == "PAID" }

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    val e = state.earnings
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard("Bookings", (e?.totalBookings ?: 0).toString(), Modifier.weight(1f))
                        StatCard("Net earnings", Money.rupees(e?.netEarnings), Modifier.weight(1f))
                        StatCard("Pending payout", Money.rupees(e?.pendingPayout), Modifier.weight(1f))
                    }
                    ErrorText(state.error, Modifier.padding(top = 8.dp))
                }
                if (needsAction.isNotEmpty()) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Needs your action (${needsAction.size})", style = MaterialTheme.typography.titleMedium)
                            OutlinedButton(onClick = onSeeBookings) { Text("Open") }
                        }
                    }
                }
                item { Text("Today", style = MaterialTheme.typography.titleMedium) }
                if (today.isEmpty()) {
                    item { Text("No bookings today.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                items(today, key = { it.id }) { b ->
                    PartnerBookingCard(b, state.busyId == b.id, onAction = { vm.act(b.id, it) })
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerBookingsScreen(api: BookingApi, modifier: Modifier = Modifier) {
    val vm: PartnerBookingsViewModel = viewModel(factory = vmFactory { PartnerBookingsViewModel(api) })
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Text("Bookings", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        PrimaryTabRow(selectedTabIndex = tab) {
            listOf("Requests", "Upcoming", "Past").forEachIndexed { i, t ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
            }
        }
        ErrorText(state.error, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        LoadContent(state.bookings, onRetry = vm::refresh) { all ->
            val list = when (tab) {
                0 -> all.filter { it.status == "PENDING" && it.paymentStatus == "PAID" }.sortedBy { it.startAt }
                1 -> all.filter { it.status == "CONFIRMED" }.sortedBy { it.startAt }
                else -> all.filter { it.status in listOf("COMPLETED", "CANCELLED", "NO_SHOW") }.sortedByDescending { it.startAt }
            }
            if (list.isEmpty()) {
                EmptyBox(if (tab == 0) "No booking requests waiting for you." else "Nothing here yet.")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { b ->
                        PartnerBookingCard(b, state.busyId == b.id, onAction = { vm.act(b.id, it) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerBookingCard(b: BookingDto, busy: Boolean, onAction: (String) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(b.customerName ?: "Customer", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusChip(b.status)
            }
            Text(b.serviceName ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(DateUtils.dateTime(b.startAt))
            val kind = when (b.bookingType) { "DEMO" -> " • Free demo"; "RECURRING" -> " • Weekly class"; else -> "" }
            Text(
                (if (b.amount == 0.0) "Free" else Money.rupees(b.amount)) + kind,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val canAccept = b.status == "PENDING" && b.paymentStatus == "PAID"
            val canReject = b.status in listOf("PENDING", "CONFIRMED")
            // A class can be marked complete once it has started.
            val canComplete = b.status == "CONFIRMED" && !DateUtils.isFuture(b.startAt)
            if (canAccept || canReject || canComplete) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canAccept) Button(enabled = !busy, onClick = { onAction("accept") }) { Text("Accept") }
                    if (canComplete) Button(enabled = !busy, onClick = { onAction("complete") }) { Text("Mark done") }
                    if (canReject) OutlinedButton(enabled = !busy, onClick = { onAction("reject") }) {
                        Text(if (b.status == "PENDING") "Reject" else "Cancel")
                    }
                }
            }
        }
    }
}
