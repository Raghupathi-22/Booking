package com.hyderabad.booking.partner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hyderabad.booking.common.AppContainer
import com.hyderabad.booking.common.model.PartnerDto
import com.hyderabad.booking.common.model.PartnerEarningsDto
import com.hyderabad.booking.common.model.PayoutDto
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.LabeledRow
import com.hyderabad.booking.common.ui.RatingText
import com.hyderabad.booking.common.ui.SectionTitle
import com.hyderabad.booking.common.ui.StatusChip
import com.hyderabad.booking.common.util.Money

@Composable
fun AccountScreen(container: AppContainer, partner: PartnerDto, modifier: Modifier = Modifier) {
    var earnings by remember { mutableStateOf<PartnerEarningsDto?>(null) }
    var payouts by remember { mutableStateOf<List<PayoutDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        (safeApiCall { container.api.earnings() } as? ApiResult.Success)?.let { earnings = it.data }
        (safeApiCall { container.api.payouts() } as? ApiResult.Success)?.let { payouts = it.data }
    }

    Column(modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Account", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(partner.businessName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            StatusChip(partner.status ?: "")
        }
        Text(listOfNotNull(partner.area, partner.address).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant)
        RatingText(partner.ratingAvg, partner.ratingCount, Modifier.padding(top = 4.dp))
        Text(container.tokenStore.user?.phone.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

        SectionTitle("Earnings")
        earnings?.let {
            LabeledRow("Completed bookings", it.totalBookings.toString())
            LabeledRow("Total revenue", Money.rupees(it.totalRevenue))
            LabeledRow("Platform commission", Money.rupees(it.totalCommission))
            LabeledRow("Net earnings", Money.rupees(it.netEarnings))
            LabeledRow("Pending payout", Money.rupees(it.pendingPayout))
        } ?: Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionTitle("Payout history")
        if (payouts.isEmpty()) Text("No payouts yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        payouts.forEach {
            LabeledRow("${it.periodStart.orEmpty()} → ${it.periodEnd.orEmpty()}", "${Money.rupees(it.amount)} • ${it.status.orEmpty().lowercase()}")
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp))
        OutlinedButton(onClick = { container.tokenStore.clear() }, modifier = Modifier.fillMaxWidth()) { Text("Log out") }
    }
}
