package com.hyderabad.booking.partner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.model.ServiceDto
import com.hyderabad.booking.common.model.ServiceRequest
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.AppTopBar
import com.hyderabad.booking.common.ui.EmptyBox
import com.hyderabad.booking.common.ui.ErrorText
import com.hyderabad.booking.common.ui.LoadContent
import com.hyderabad.booking.common.util.Load
import com.hyderabad.booking.common.util.Money
import com.hyderabad.booking.common.util.pretty
import com.hyderabad.booking.common.util.toLoad
import com.hyderabad.booking.common.util.vmFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServicesViewModel(private val api: BookingApi) : ViewModel() {
    data class State(val services: Load<List<ServiceDto>> = Load.Loading, val error: String? = null)

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(services = safeApiCall { api.partnerServices() }.toLoad()) }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            when (val r = safeApiCall { api.deleteService(id) }) {
                is ApiResult.Success -> refresh()
                is ApiResult.Error -> _state.update { it.copy(error = r.message) }
            }
        }
    }
}

@Composable
fun ServicesScreen(api: BookingApi, modifier: Modifier = Modifier, onEdit: (serviceId: Long) -> Unit) {
    val vm: ServicesViewModel = viewModel(factory = vmFactory { ServicesViewModel(api) })
    val state by vm.state.collectAsStateWithLifecycle()
    var toDelete by rememberSaveable { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) { vm.refresh() }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text("My services", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
            ErrorText(state.error, Modifier.padding(horizontal = 16.dp))
            LoadContent(state.services, onRetry = vm::refresh) { list ->
                if (list.isEmpty()) {
                    EmptyBox("Add your first service so customers can book you.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(list, key = { it.id }) { s ->
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(s.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        Text(Money.rupees(s.price), style = MaterialTheme.typography.titleMedium)
                                    }
                                    Text(
                                        "${s.durationMinutes} min • ${(s.mode ?: "").pretty()}" + if (s.active == false) " • Hidden" else "",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { onEdit(s.id) }) { Text("Edit") }
                                        TextButton(onClick = { toDelete = s.id }) { Text("Delete") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { onEdit(0L) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = "Add service") }
    }

    toDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete service?") },
            text = { Text("Customers will no longer be able to book it.") },
            confirmButton = { TextButton(onClick = { vm.delete(id); toDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Keep") } }
        )
    }
}

@Composable
fun ServiceEditScreen(api: BookingApi, serviceId: Long, defaultCategory: String, onBack: () -> Unit) {
    var loaded by remember { mutableStateOf(serviceId == 0L) }
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("60") }
    var mode by rememberSaveable { mutableStateOf(if (defaultCategory == "TUITION") "ONLINE" else "AT_SHOP") }
    var active by rememberSaveable { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // When editing, fill the form from the saved service (only the first time).
    LaunchedEffect(serviceId) {
        if (serviceId != 0L && !loaded) {
            when (val r = safeApiCall { api.partnerServices() }) {
                is ApiResult.Success -> r.data.firstOrNull { it.id == serviceId }?.let {
                    name = it.name; description = it.description.orEmpty()
                    price = if (it.price % 1.0 == 0.0) it.price.toLong().toString() else it.price.toString()
                    duration = it.durationMinutes.toString(); mode = it.mode ?: mode; active = it.active ?: true
                }
                is ApiResult.Error -> error = r.message
            }
            loaded = true
        }
    }

    Scaffold(topBar = { AppTopBar(if (serviceId == 0L) "Add service" else "Edit service", onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Service name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(description, { description = it }, label = { Text("Description (optional)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    price, { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Price (₹)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    duration, { duration = it.filter(Char::isDigit) },
                    label = { Text("Minutes") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("Where is it done?", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("AT_SHOP", "AT_HOME", "ONLINE").forEach { m ->
                    FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(m.pretty()) })
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Visible to customers", modifier = Modifier.weight(1f))
                Switch(checked = active, onCheckedChange = { active = it })
            }
            ErrorText(error, Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = !busy && loaded && name.isNotBlank() && price.toDoubleOrNull() != null && (duration.toIntOrNull() ?: 0) > 0,
                onClick = {
                    scope.launch {
                        busy = true; error = null
                        val req = ServiceRequest(
                            category = defaultCategory, name = name.trim(),
                            description = description.trim().ifBlank { null },
                            price = price.toDouble(), durationMinutes = duration.toInt(),
                            mode = mode, active = active
                        )
                        val r = safeApiCall { if (serviceId == 0L) api.createService(req) else api.updateService(serviceId, req) }
                        when (r) {
                            is ApiResult.Success -> onBack()
                            is ApiResult.Error -> error = r.message
                        }
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Save service") }
        }
    }
}
