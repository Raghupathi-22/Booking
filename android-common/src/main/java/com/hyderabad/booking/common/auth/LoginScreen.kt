package com.hyderabad.booking.common.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hyderabad.booking.common.ui.ErrorText

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        if (!state.otpSent) {
            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Mobile number") },
                prefix = { Text("+91 ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Your name (new users)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text("Enter the 6 digit OTP sent to +91 ${state.phone}")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.otp,
                onValueChange = viewModel::onOtpChange,
                label = { Text("OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )
            // The development backend returns the OTP in its response so you can test without SMS.
            state.devOtp?.let {
                Spacer(Modifier.height(8.dp))
                Text("Dev OTP: $it", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
            }
        }

        ErrorText(state.error, Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { if (state.otpSent) viewModel.verifyOtp() else viewModel.sendOtp() },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (state.loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(if (state.otpSent) "Verify & continue" else "Send OTP")
            }
        }
        if (state.otpSent) {
            TextButton(onClick = viewModel::changeNumber, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Change number")
            }
        }
    }
}
