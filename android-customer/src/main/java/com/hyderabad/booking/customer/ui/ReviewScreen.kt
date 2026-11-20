package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hyderabad.booking.common.model.ReviewRequest
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.AppTopBar
import com.hyderabad.booking.common.ui.ErrorText
import kotlinx.coroutines.launch

@Composable
fun ReviewScreen(
    api: BookingApi,
    bookingId: Long,
    businessName: String,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var comment by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { AppTopBar("Rate your visit", onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(businessName, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "$star stars",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it.take(1000) },
                label = { Text("Tell others about your experience (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            ErrorText(error)
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = rating in 1..5 && !busy,
                onClick = {
                    scope.launch {
                        busy = true; error = null
                        when (val r = safeApiCall { api.createReview(ReviewRequest(bookingId, rating, comment.trim().ifBlank { null })) }) {
                            is ApiResult.Success -> onDone()
                            is ApiResult.Error -> error = r.message
                        }
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Submit review") }
        }
    }
}
