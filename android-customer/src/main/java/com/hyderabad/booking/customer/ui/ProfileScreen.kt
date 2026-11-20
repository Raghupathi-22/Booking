package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hyderabad.booking.common.AppContainer
import com.hyderabad.booking.common.model.UpdateUserRequest
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.safeApiCall
import com.hyderabad.booking.common.ui.ErrorText
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(container: AppContainer, modifier: Modifier = Modifier) {
    val user = container.tokenStore.user
    var name by remember { mutableStateOf(user?.name.orEmpty()) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Mobile", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(user?.phone.orEmpty(), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; message = null },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        ErrorText(error)
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = !saving && name.isNotBlank(),
            onClick = {
                scope.launch {
                    saving = true; error = null
                    when (val r = safeApiCall { container.api.updateMe(UpdateUserRequest(name = name.trim())) }) {
                        is ApiResult.Success -> { container.tokenStore.updateName(r.data.name); message = "Saved" }
                        is ApiResult.Error -> error = r.message
                    }
                    saving = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save") }
        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = { container.tokenStore.clear() }, modifier = Modifier.fillMaxWidth()) {
            Text("Log out")
        }
    }
}
