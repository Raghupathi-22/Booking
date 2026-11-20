package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.model.BookingRequest
import com.hyderabad.booking.common.model.PartnerDetailDto
import com.hyderabad.booking.common.model.ServiceDto
import com.hyderabad.booking.common.model.SlotDto
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.AppTopBar
import com.hyderabad.booking.common.ui.ErrorText
import com.hyderabad.booking.common.ui.LabeledRow
import com.hyderabad.booking.common.ui.LoadContent
import com.hyderabad.booking.common.ui.SectionTitle
import com.hyderabad.booking.common.util.DateUtils
import com.hyderabad.booking.common.util.Load
import com.hyderabad.booking.common.util.Money
import com.hyderabad.booking.common.util.pretty
import com.hyderabad.booking.common.util.vmFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookState(
    val info: Load<Pair<PartnerDetailDto, ServiceDto>> = Load.Loading,
    val dates: List<String> = DateUtils.nextDays(14),
    val selectedDate: String = dates.first(),
    val slots: Load<List<SlotDto>> = Load.Loading,
    val selectedSlot: SlotDto? = null,
    val bookingType: String = "SINGLE",     // SINGLE | DEMO | RECURRING
    val creating: Boolean = false,
    val error: String? = null
)

class BookViewModel(
    private val api: BookingApi,
    private val partnerId: Long,
    private val serviceId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(BookState())
    val state: StateFlow<BookState> = _state

    /** Called every time the screen opens so old selections never leak into a new booking. */
    fun start() {
        _state.value = BookState()
        viewModelScope.launch {
            when (val r = safeApiCall { api.partnerDetail(partnerId) }) {
                is ApiResult.Success -> {
                    val service = r.data.services.firstOrNull { it.id == serviceId }
                    if (service == null) {
                        _state.update { it.copy(info = Load.Failed("This service is no longer available.")) }
                    } else {
                        _state.update { it.copy(info = Load.Ready(r.data to service)) }
                        loadSlots()
                    }
                }
                is ApiResult.Error -> _state.update { it.copy(info = Load.Failed(r.message)) }
            }
        }
    }

    fun selectDate(date: String) {
        _state.update { it.copy(selectedDate = date, selectedSlot = null, error = null) }
        loadSlots()
    }

    fun selectSlot(slot: SlotDto) = _state.update { it.copy(selectedSlot = slot, error = null) }
    fun selectType(type: String) = _state.update { it.copy(bookingType = type, error = null) }

    fun loadSlots() {
        val date = _state.value.selectedDate
        viewModelScope.launch {
            _state.update { it.copy(slots = Load.Loading) }
            when (val r = safeApiCall { api.slots(partnerId, date, serviceId) }) {
                is ApiResult.Success -> {
                    // Ignore a late response for a date the user has already moved away from.
                    if (_state.value.selectedDate == date) {
                        val upcoming = r.data.filter { DateUtils.isFuture(it.startAt) }
                        _state.update { it.copy(slots = Load.Ready(upcoming)) }
                    }
                }
                is ApiResult.Error -> _state.update { it.copy(slots = Load.Failed(r.message)) }
            }
        }
    }

    fun confirm(onPaid: (List<Long>) -> Unit, onFree: () -> Unit) {
        val s = _state.value
        val slot = s.selectedSlot ?: return
        viewModelScope.launch {
            _state.update { it.copy(creating = true, error = null) }
            val request = BookingRequest(
                partnerId = partnerId,
                serviceId = serviceId,
                startAt = slot.startAt,
                bookingType = s.bookingType,
                recurrenceRule = if (s.bookingType == "RECURRING") "FREQ=WEEKLY;WEEKS=4" else null
            )
            when (val r = safeApiCall { api.createBooking(request) }) {
                is ApiResult.Success -> {
                    _state.update { it.copy(creating = false) }
                    if (s.bookingType == "DEMO") onFree() else onPaid(r.data.map { it.id })
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(creating = false, error = r.message) }
                    loadSlots() // the slot may have just been taken: refresh the list
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookScreen(
    api: BookingApi,
    partnerId: Long,
    serviceId: Long,
    onBack: () -> Unit,
    onCheckout: (List<Long>) -> Unit,
    onFreeBooked: () -> Unit
) {
    val vm: BookViewModel = viewModel(
        key = "book_${partnerId}_$serviceId",
        factory = vmFactory { BookViewModel(api, partnerId, serviceId) }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.start() }

    Scaffold(topBar = { AppTopBar("Book", onBack) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            LoadContent(state.info, onRetry = vm::start) { (partner, service) ->
                Column(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        Text(partner.businessName, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${service.name} • ${service.durationMinutes} min • ${Money.rupees(service.price)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (service.category == "TUITION") {
                            SectionTitle("Class type")
                            TypeOption("SINGLE", "Single class", "Pay ${Money.rupees(service.price)} for one class", state.bookingType, vm::selectType)
                            TypeOption("DEMO", "Free demo class", "Try the first class for free", state.bookingType, vm::selectType)
                            TypeOption("RECURRING", "Weekly classes (4 weeks)", "Same time every week • ${Money.rupees(service.price * 4)} in total", state.bookingType, vm::selectType)
                        }

                        SectionTitle("Pick a date")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.dates) { date ->
                                val (day, num) = DateUtils.chipParts(date)
                                val selected = date == state.selectedDate
                                OutlinedCard(
                                    modifier = Modifier.clickable { vm.selectDate(date) },
                                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(day, style = MaterialTheme.typography.labelMedium)
                                        Text(num, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }

                        SectionTitle("Pick a time")
                        LoadContent(state.slots, onRetry = vm::loadSlots) { slots ->
                            if (slots.isEmpty()) {
                                Text("No free slots on this day. Try another date.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    slots.forEach { slot ->
                                        FilterChip(
                                            selected = slot == state.selectedSlot,
                                            onClick = { vm.selectSlot(slot) },
                                            label = { Text(DateUtils.time(slot.startAt)) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom summary + confirm
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        state.selectedSlot?.let {
                            LabeledRow("When", DateUtils.dateTime(it.startAt))
                            val total = when (state.bookingType) {
                                "DEMO" -> 0.0
                                "RECURRING" -> service.price * 4
                                else -> service.price
                            }
                            LabeledRow("Total", if (total == 0.0) "Free" else Money.rupees(total))
                        }
                        ErrorText(state.error)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { vm.confirm(onPaid = onCheckout, onFree = onFreeBooked) },
                            enabled = state.selectedSlot != null && !state.creating,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (state.creating) {
                                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text(if (state.bookingType == "DEMO") "Request free demo" else "Continue to payment")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeOption(value: String, title: String, subtitle: String, selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onSelect(value) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected == value, onClick = { onSelect(value) })
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
