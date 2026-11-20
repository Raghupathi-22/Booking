package com.hyderabad.booking.partner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hyderabad.booking.common.model.TimeOffDto
import com.hyderabad.booking.common.model.TimeOffRequest
import com.hyderabad.booking.common.model.WorkingHoursRequest
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.ErrorText
import com.hyderabad.booking.common.ui.LoadingBox
import com.hyderabad.booking.common.util.DAYS_OF_WEEK
import com.hyderabad.booking.common.util.DateUtils
import com.hyderabad.booking.common.util.pretty
import kotlinx.coroutines.launch

private class DayHours(open: Boolean, from: String, to: String) {
    var open by mutableStateOf(open)
    var from by mutableStateOf(from)
    var to by mutableStateOf(to)
}

/** 06:00, 06:30 ... 23:30 */
private val TIME_OPTIONS = (6..23).flatMap { h -> listOf("%02d:00".format(h), "%02d:30".format(h)) }

@Composable
fun ScheduleScreen(api: BookingApi, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val days = remember { DAYS_OF_WEEK.associateWith { DayHours(false, "09:00", "18:00") } }
    val timeOff = remember { mutableStateListOf<TimeOffDto>() }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    // time-off form
    val dates = remember { DateUtils.nextDays(30) }
    var offDate by remember { mutableStateOf(dates.first()) }
    var offFrom by remember { mutableStateOf("09:00") }
    var offTo by remember { mutableStateOf("18:00") }

    LaunchedEffect(Unit) {
        (safeApiCall { api.workingHours() } as? ApiResult.Success)?.data?.forEach { h ->
            days[h.dayOfWeek.uppercase()]?.apply { open = true; from = h.openTime.take(5); to = h.closeTime.take(5) }
        }
        (safeApiCall { api.timeOff() } as? ApiResult.Success)?.data?.let { timeOff.clear(); timeOff.addAll(it) }
        loading = false
    }

    if (loading) { LoadingBox(modifier); return }

    Column(modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Working hours", style = MaterialTheme.typography.headlineSmall)
        Text("Customers can only book inside these hours.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        DAYS_OF_WEEK.forEach { day ->
            val d = days.getValue(day)
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = d.open, onCheckedChange = { d.open = it; message = null })
                Text(day.pretty().take(3), modifier = Modifier.padding(horizontal = 12.dp).weight(0.6f))
                if (d.open) {
                    TimeChoice(d.from) { d.from = it; message = null }
                    Text(" – ", modifier = Modifier.padding(horizontal = 4.dp))
                    TimeChoice(d.to) { d.to = it; message = null }
                } else {
                    Text("Closed", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        ErrorText(error)
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(8.dp))
        Button(
            enabled = !saving,
            onClick = {
                val bad = days.entries.firstOrNull { it.value.open && it.value.from >= it.value.to }
                if (bad != null) { error = "${bad.key.pretty()}: closing time must be after opening time"; return@Button }
                scope.launch {
                    saving = true; error = null; message = null
                    val body = days.entries.filter { it.value.open }
                        .map { WorkingHoursRequest(it.key, "${it.value.from}:00", "${it.value.to}:00") }
                    when (val r = safeApiCall { api.setWorkingHours(body) }) {
                        is ApiResult.Success -> message = "Working hours saved"
                        is ApiResult.Error -> error = r.message
                    }
                    saving = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Save working hours") }

        HorizontalDivider(Modifier.padding(vertical = 24.dp))

        Text("Time off", style = MaterialTheme.typography.titleLarge)
        Text("Block a day or part of a day (holiday, leave).", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DateChoice(offDate, dates) { offDate = it }
            Spacer(Modifier.padding(4.dp))
            TimeChoice(offFrom) { offFrom = it }
            Text(" – ")
            TimeChoice(offTo) { offTo = it }
        }
        OutlinedButton(
            onClick = {
                scope.launch {
                    error = null
                    val req = TimeOffRequest(DateUtils.join(offDate, offFrom), DateUtils.join(offDate, offTo))
                    when (val r = safeApiCall { api.addTimeOff(req) }) {
                        is ApiResult.Success -> timeOff.add(r.data)
                        is ApiResult.Error -> error = r.message
                    }
                }
            },
            enabled = offFrom < offTo,
            modifier = Modifier.padding(top = 8.dp)
        ) { Text("Add time off") }

        Spacer(Modifier.height(12.dp))
        if (timeOff.isEmpty()) Text("No time off added.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        timeOff.sortedBy { it.startAt }.forEach {
            Text("${DateUtils.dayLabel(it.startAt)}, ${DateUtils.time(it.startAt)} – ${DateUtils.time(it.endAt)}", modifier = Modifier.padding(vertical = 2.dp))
        }
    }
}

@Composable
private fun TimeChoice(value: String, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(DateUtils.clock(value)) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            TIME_OPTIONS.forEach { t ->
                DropdownMenuItem(text = { Text(DateUtils.clock(t)) }, onClick = { onChange(t); open = false })
            }
        }
    }
}

@Composable
private fun DateChoice(value: String, options: List<String>, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(DateUtils.dayLabel("${value}T00:00:00")) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { d ->
                DropdownMenuItem(text = { Text(DateUtils.dayLabel("${d}T00:00:00")) }, onClick = { onChange(d); open = false })
            }
        }
    }
}
