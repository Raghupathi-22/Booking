package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.AppContainer
import com.hyderabad.booking.common.model.PartnerDto
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.EmptyBox
import com.hyderabad.booking.common.ui.LoadContent
import com.hyderabad.booking.common.ui.RatingText
import com.hyderabad.booking.common.ui.SectionTitle
import com.hyderabad.booking.common.ui.StatusChip
import com.hyderabad.booking.common.util.Load
import com.hyderabad.booking.common.util.toLoad
import com.hyderabad.booking.common.util.vmFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val api: BookingApi) : ViewModel() {
    private val _top = MutableStateFlow<Load<List<PartnerDto>>>(Load.Loading)
    val top: StateFlow<Load<List<PartnerDto>>> = _top

    init { load() }

    fun load() {
        viewModelScope.launch {
            _top.value = Load.Loading
            _top.value = safeApiCall { api.searchPartners(sort = "rating") }.toLoad()
        }
    }
}

@Composable
fun HomeScreen(
    container: AppContainer,
    modifier: Modifier = Modifier,
    onSearch: (category: String?, area: String, title: String) -> Unit,
    onOpenPartner: (Long) -> Unit
) {
    val vm: HomeViewModel = viewModel(factory = vmFactory { HomeViewModel(container.api) })
    val top by vm.top.collectAsStateWithLifecycle()
    var area by rememberSaveable { mutableStateOf("") }
    val name = container.tokenStore.user?.name?.takeIf { it.isNotBlank() && !it.startsWith("User ") }

    LazyColumn(
        modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            Text(
                if (name != null) "Hi $name 👋" else "Hello 👋",
                style = MaterialTheme.typography.headlineSmall
            )
            Text("What would you like to book?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Area in Hyderabad (e.g. Madhapur)") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { onSearch(null, area.trim(), "Search results") }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryCard("Salons", Icons.Filled.ContentCut, Modifier.weight(1f)) {
                    onSearch("SALON", area.trim(), "Salons")
                }
                CategoryCard("Tutors", Icons.Filled.School, Modifier.weight(1f)) {
                    onSearch("TUITION", area.trim(), "Tutors")
                }
            }
            SectionTitle("Top rated")
        }
        item {
            LoadContent(top, onRetry = vm::load) { partners ->
                if (partners.isEmpty()) {
                    EmptyBox("No businesses have joined yet. Check back soon!", Modifier.height(160.dp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        partners.take(10).forEach { PartnerCard(it) { onOpenPartner(it.id) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun PartnerCard(partner: PartnerDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(partner.businessName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatusChip(if (partner.type == "TUTOR") "Tutor" else "Salon")
            }
            Spacer(Modifier.height(4.dp))
            Text(partner.area ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            RatingText(partner.ratingAvg, partner.ratingCount)
        }
    }
}
