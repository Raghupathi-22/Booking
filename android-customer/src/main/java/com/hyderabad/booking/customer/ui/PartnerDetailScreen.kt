package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.model.PartnerDetailDto
import com.hyderabad.booking.common.model.ServiceDto
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.AppTopBar
import com.hyderabad.booking.common.ui.LoadContent
import com.hyderabad.booking.common.ui.RatingText
import com.hyderabad.booking.common.ui.SectionTitle
import com.hyderabad.booking.common.util.DAYS_OF_WEEK
import com.hyderabad.booking.common.util.DateUtils
import com.hyderabad.booking.common.util.Load
import com.hyderabad.booking.common.util.Money
import com.hyderabad.booking.common.util.pretty
import com.hyderabad.booking.common.util.toLoad
import com.hyderabad.booking.common.util.vmFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PartnerDetailViewModel(private val api: BookingApi, private val partnerId: Long) : ViewModel() {
    private val _state = MutableStateFlow<Load<PartnerDetailDto>>(Load.Loading)
    val state: StateFlow<Load<PartnerDetailDto>> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = Load.Loading
            _state.value = safeApiCall { api.partnerDetail(partnerId) }.toLoad()
        }
    }
}

@Composable
fun PartnerDetailScreen(
    api: BookingApi,
    partnerId: Long,
    onBack: () -> Unit,
    onBook: (serviceId: Long) -> Unit
) {
    val vm: PartnerDetailViewModel = viewModel(
        key = "partner_$partnerId",
        factory = vmFactory { PartnerDetailViewModel(api, partnerId) }
    )
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar("Details", onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LoadContent(state, onRetry = vm::load) { d -> DetailContent(d, onBook) }
        }
    }
}

@Composable
private fun DetailContent(d: PartnerDetailDto, onBook: (Long) -> Unit) {
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        item {
            Text(d.businessName, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            RatingText(d.ratingAvg, d.ratingCount)
            Spacer(Modifier.height(8.dp))
            val place = listOfNotNull(d.address, d.area).filter { it.isNotBlank() }.joinToString(", ")
            if (place.isNotBlank()) Text(place, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!d.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(d.description!!)
            }
            SectionTitle("Services")
        }
        if (d.services.isEmpty()) {
            item { Text("No services listed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(d.services.size) { i ->
            ServiceRow(d.services[i], onBook)
            Spacer(Modifier.height(8.dp))
        }

        item {
            SectionTitle("Opening hours")
            if (d.workingHours.isEmpty()) {
                Text("Not set", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                DAYS_OF_WEEK.forEach { day ->
                    val h = d.workingHours.filter { it.dayOfWeek.equals(day, true) }
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(day.pretty())
                        Text(
                            if (h.isEmpty()) "Closed"
                            else h.joinToString(", ") { "${DateUtils.clock(it.openTime)} – ${DateUtils.clock(it.closeTime)}" },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            SectionTitle("Reviews")
            if (d.reviews.isEmpty()) {
                Text("No reviews yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(d.reviews.size) { i ->
            val r = d.reviews[i]
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(r.customerName ?: "Customer", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(8.dp))
                    Text("★".repeat(r.rating.coerceIn(0, 5)), color = androidx.compose.ui.graphics.Color(0xFFFFB300))
                }
                if (!r.comment.isNullOrBlank()) Text(r.comment!!, style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun ServiceRow(s: ServiceDto, onBook: (Long) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${s.durationMinutes} min • ${(s.mode ?: "").pretty()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!s.description.isNullOrBlank()) {
                    Text(s.description!!, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Money.rupees(s.price), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Button(onClick = { onBook(s.id) }) { Text("Book") }
            }
        }
    }
}
