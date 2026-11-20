package com.hyderabad.booking.common.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyderabad.booking.common.model.SendOtpRequest
import com.hyderabad.booking.common.model.VerifyOtpRequest
import com.hyderabad.booking.common.network.ApiResult
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.TokenStore
import com.hyderabad.booking.common.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phone: String = "",
    val name: String = "",
    val otp: String = "",
    val otpSent: Boolean = false,
    val devOtp: String? = null,
    val loading: Boolean = false,
    val error: String? = null
)

/**
 * Phone + OTP login shared by both apps.
 * @param role role given to NEW accounts ("CUSTOMER" or "PARTNER").
 * @param blockedRoles roles that must not use this app (the session is cleared with a message).
 */
class AuthViewModel(
    private val api: BookingApi,
    private val tokenStore: TokenStore,
    private val role: String,
    private val blockedRoles: Set<String>,
    private val blockedMessage: String
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v.filter(Char::isDigit).take(12), error = null) }
    fun onNameChange(v: String) = _state.update { it.copy(name = v, error = null) }
    fun onOtpChange(v: String) = _state.update { it.copy(otp = v.filter(Char::isDigit).take(6), error = null) }
    fun changeNumber() = _state.update { it.copy(otpSent = false, otp = "", devOtp = null, error = null) }

    /** 10 digit Indian numbers get +91; anything longer is sent as typed with a leading +. */
    private fun fullPhone(): String {
        val digits = _state.value.phone
        return if (digits.length == 10) "+91$digits" else "+$digits"
    }

    fun sendOtp() {
        val digits = _state.value.phone
        if (digits.length < 10) {
            _state.update { it.copy(error = "Enter a valid 10 digit mobile number") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = safeApiCall { api.sendOtp(SendOtpRequest(fullPhone())) }) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, otpSent = true, devOtp = r.data.devOtp, otp = "")
                }
                is ApiResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }

    fun verifyOtp() {
        val s = _state.value
        if (s.otp.length != 6) {
            _state.update { it.copy(error = "Enter the 6 digit OTP") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val request = VerifyOtpRequest(
                phone = fullPhone(),
                otp = s.otp,
                name = s.name.trim().ifBlank { null },
                role = role
            )
            when (val r = safeApiCall { api.verifyOtp(request) }) {
                is ApiResult.Success -> {
                    if (r.data.user.role in blockedRoles) {
                        _state.update { it.copy(loading = false, otpSent = false, otp = "", devOtp = null, error = blockedMessage) }
                    } else {
                        tokenStore.save(r.data) // flips tokenStore.loggedIn -> the app shows its home screen
                        _state.value = AuthUiState()
                    }
                }
                is ApiResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }
}
