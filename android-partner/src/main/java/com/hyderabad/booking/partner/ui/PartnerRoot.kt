package com.hyderabad.booking.partner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.AppContainer
import com.hyderabad.booking.common.auth.AuthViewModel
import com.hyderabad.booking.common.auth.LoginScreen
import com.hyderabad.booking.common.model.PartnerDocumentRequest
import com.hyderabad.booking.common.model.PartnerDto
import com.hyderabad.booking.common.model.PartnerRegisterRequest
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.ErrorBox
import com.hyderabad.booking.common.ui.ErrorText
import com.hyderabad.booking.common.ui.LoadingBox
import com.hyderabad.booking.common.ui.StatusChip
import com.hyderabad.booking.common.ui.rememberBackStack
import com.hyderabad.booking.common.util.vmFactory
import com.hyderabad.booking.partner.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun PartnerRoot(container: AppContainer) {
    val loggedIn by container.tokenStore.loggedIn.collectAsStateWithLifecycle()
    if (!loggedIn) {
        val vm: AuthViewModel = viewModel(
            factory = vmFactory {
                AuthViewModel(
                    api = container.api,
                    tokenStore = container.tokenStore,
                    role = "PARTNER",
                    blockedRoles = setOf("ADMIN"),
                    blockedMessage = "Admin accounts cannot use the partner app."
                )
            }
        )
        LoginScreen(vm, "Partner login", "Manage your salon or tuition bookings")
    } else {
        PartnerGate(container)
    }
}

// ---------- Gate: decides between registration, waiting for approval and the real app ----------

sealed interface GateState {
    data object Loading : GateState
    data object NeedsRegistration : GateState
    data class Failed(val message: String) : GateState
    data class Known(val partner: PartnerDto) : GateState
}

class GateViewModel(private val api: BookingApi) : ViewModel() {
    private val _state = MutableStateFlow<GateState>(GateState.Loading)
    val state: StateFlow<GateState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = GateState.Loading
            _state.value = when (val r = safeApiCall { api.partnerMe() }) {
                is ApiResult.Success -> GateState.Known(r.data)
                is ApiResult.Error -> if (r.code == 404) GateState.NeedsRegistration else GateState.Failed(r.message)
            }
        }
    }
}

@Composable
private fun PartnerGate(container: AppContainer) {
    val vm: GateViewModel = viewModel(factory = vmFactory { GateViewModel(container.api) })
    val state by vm.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is GateState.Loading -> LoadingBox()
        is GateState.Failed -> ErrorBox(s.message, onRetry = vm::load)
        is GateState.NeedsRegistration -> RegisterScreen(container, onRegistered = vm::load)
        is GateState.Known ->
            if (s.partner.status == "APPROVED") PartnerMain(container, s.partner)
            else StatusScreen(container, s.partner, onRefresh = vm::load)
    }
}

// ---------- Registration ----------

@Composable
private fun RegisterScreen(container: AppContainer, onRegistered: () -> Unit) {
    var type by rememberSaveable { mutableStateOf("SALON") }
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("Register your business", style = MaterialTheme.typography.headlineSmall)
        Text("Our team will review it before customers can book you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = type == "SALON", onClick = { type = "SALON" }, label = { Text("Salon") })
            FilterChip(selected = type == "TUTOR", onClick = { type = "TUTOR" }, label = { Text("Tutor") })
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Business name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(area, { area = it }, label = { Text("Area (e.g. Madhapur)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(address, { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(description, { description = it }, label = { Text("About your business") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        ErrorText(error, Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = !busy && name.isNotBlank() && area.isNotBlank(),
            onClick = {
                scope.launch {
                    busy = true; error = null
                    val req = PartnerRegisterRequest(
                        type = type, businessName = name.trim(), area = area.trim(),
                        address = address.trim().ifBlank { null }, description = description.trim().ifBlank { null }
                    )
                    when (val r = safeApiCall { container.api.registerPartner(req) }) {
                        is ApiResult.Success -> onRegistered()
                        is ApiResult.Error -> error = r.message
                    }
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Submit for approval") }
        OutlinedButton(onClick = { container.tokenStore.clear() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Log out") }
    }
}

// ---------- Waiting for approval / rejected ----------

@Composable
private fun StatusScreen(container: AppContainer, partner: PartnerDto, onRefresh: () -> Unit) {
    var docType by rememberSaveable { mutableStateOf("ID_PROOF") }
    var url by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(partner.businessName, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        StatusChip(partner.status ?: "PENDING")
        Spacer(Modifier.height(12.dp))
        Text(
            when (partner.status) {
                "REJECTED" -> "Your application was not approved. Please contact support."
                "SUSPENDED" -> "Your account is suspended. Please contact support."
                else -> "Your business is waiting for approval. You can add verification documents while you wait."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        if (partner.status == "PENDING") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ID_PROOF" to "ID proof", "SHOP_PHOTO" to "Shop photo", "CERTIFICATE" to "Certificate").forEach { (v, l) ->
                    FilterChip(selected = docType == v, onClick = { docType = v }, label = { Text(l) })
                }
            }
            OutlinedTextField(
                url, { url = it }, label = { Text("Link to the document / photo") },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            ErrorText(error)
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Button(
                enabled = url.isNotBlank(),
                onClick = {
                    scope.launch {
                        error = null; message = null
                        when (val r = safeApiCall { container.api.uploadDocument(PartnerDocumentRequest(docType, url.trim())) }) {
                            is ApiResult.Success -> { message = "Document added"; url = "" }
                            is ApiResult.Error -> error = r.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Add document") }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("Check status") }
        OutlinedButton(onClick = { container.tokenStore.clear() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Log out") }
    }
}

// ---------- Main app (approved partners) ----------

@Composable
private fun PartnerMain(container: AppContainer, partner: PartnerDto) {
    val stack = rememberBackStack<Screen>(Screen.Main())
    val api = container.api

    when (val screen = stack.current) {
        is Screen.ServiceEdit -> ServiceEditScreen(
            api = api,
            serviceId = screen.serviceId,
            defaultCategory = if (partner.type == "TUTOR") "TUITION" else "SALON",
            onBack = stack::pop
        )
        is Screen.Main -> Scaffold(
            bottomBar = {
                NavigationBar {
                    val items = listOf(
                        "Home" to Icons.Filled.Home,
                        "Bookings" to Icons.Filled.DateRange,
                        "Services" to Icons.Filled.ContentCut,
                        "Hours" to Icons.Filled.Schedule,
                        "Account" to Icons.Filled.AccountCircle
                    )
                    items.forEachIndexed { i, (label, icon) ->
                        NavigationBarItem(
                            selected = screen.tab == i,
                            onClick = { stack.replace(Screen.Main(i)) },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(label, maxLines = 1) }
                        )
                    }
                }
            }
        ) { padding ->
            val m = Modifier.padding(padding)
            when (screen.tab) {
                0 -> DashboardScreen(api, m, onSeeBookings = { stack.replace(Screen.Main(1)) })
                1 -> PartnerBookingsScreen(api, m)
                2 -> ServicesScreen(api, m, onEdit = { stack.push(Screen.ServiceEdit(it)) })
                3 -> ScheduleScreen(api, m)
                else -> AccountScreen(container, partner, m)
            }
        }
    }
}
