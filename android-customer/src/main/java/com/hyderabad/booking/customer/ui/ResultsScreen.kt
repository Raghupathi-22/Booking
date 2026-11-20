package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.model.PartnerDto
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.AppTopBar
import com.hyderabad.booking.common.ui.EmptyBox
import com.hyderabad.booking.common.ui.LoadContent
import com.hyderabad.booking.common.util.Load
import com.hyderabad.booking.common.util.toLoad
import com.hyderabad.booking.common.util.vmFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultsState(
    val area: String,
    val sort: String = "rating",       // "rating" | "price"
    val minRating: Double? = null,
    val partners: Load<List<PartnerDto>> = Load.Loading
)

class ResultsViewModel(
    private val api: BookingApi,
    private val category: String?,
    initialArea: String
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsState(area = initialArea))
    val state: StateFlow<ResultsState> = _state
    private var job: Job? = null

    init { load() }

    fun setArea(v: String) { _state.update { it.copy(area = v) }; load(debounce = true) }
    fun setSort(v: String) { _state.update { it.copy(sort = v) }; load() }
    fun setMinRating(v: Double?) { _state.update { it.copy(minRating = v) }; load() }

    fun load(debounce: Boolean = false) {
        job?.cancel()
        job = viewModelScope.launch {
            if (debounce) delay(400)
            val s = _state.value
            _state.update { it.copy(partners = Load.Loading) }
            val result = safeApiCall {
                api.searchPartners(
                    category = category,
                    area = s.area.ifBlank { null },
                    minRating = s.minRating,
                    sort = s.sort
                )
            }.toLoad()
            _state.update { it.copy(partners = result) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    api: BookingApi,
    category: String?,
    initialArea: String,
    title: String,
    onBack: () -> Unit,
    onOpenPartner: (Long) -> Unit
) {
    val vm: ResultsViewModel = viewModel(
        key = "results_${category}_$initialArea",
        factory = vmFactory { ResultsViewModel(api, category, initialArea) }
    )
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar(title, onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.area,
                onValueChange = vm::setArea,
                label = { Text("Area") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = state.sort == "rating", onClick = { vm.setSort("rating") }, label = { Text("Top rated") })
                FilterChip(selected = state.sort == "price", onClick = { vm.setSort("price") }, label = { Text("Lowest price") })
                FilterChip(
                    selected = state.minRating != null,
                    onClick = { vm.setMinRating(if (state.minRating == null) 4.0 else null) },
                    label = { Text("4★ & up") }
                )
            }
            LoadContent(state.partners, onRetry = { vm.load() }) { partners ->
                if (partners.isEmpty()) {
                    EmptyBox("No businesses found. Try a different area or filter.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(partners, key = { it.id }) { PartnerCard(it) { onOpenPartner(it.id) } }
                    }
                }
            }
        }
    }
}
