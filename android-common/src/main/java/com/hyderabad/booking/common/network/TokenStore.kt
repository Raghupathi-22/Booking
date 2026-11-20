package com.hyderabad.booking.common.network

import android.content.Context
import com.hyderabad.booking.common.dto.UserDto
import com.hyderabad.booking.common.model.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Keeps the login session on the device. [loggedIn] flips to false when the user logs out
 * or when the refresh token is rejected, so the UI can send the user back to the login screen.
 */
class TokenStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("booking_session", Context.MODE_PRIVATE)

    private val _loggedIn = MutableStateFlow(!prefs.getString(KEY_ACCESS, null).isNullOrBlank())
    val loggedIn: StateFlow<Boolean> = _loggedIn

    val accessToken: String? get() = prefs.getString(KEY_ACCESS, null)
    val refreshToken: String? get() = prefs.getString(KEY_REFRESH, null)

    val user: UserDto?
        get() {
            val id = prefs.getLong(KEY_USER_ID, -1L)
            if (id < 0) return null
            return UserDto(
                id = id,
                phone = prefs.getString(KEY_PHONE, "") ?: "",
                name = prefs.getString(KEY_NAME, "") ?: "",
                role = prefs.getString(KEY_ROLE, "") ?: ""
            )
        }

    @Synchronized
    fun save(auth: AuthResponse) {
        prefs.edit()
            .putString(KEY_ACCESS, auth.accessToken)
            .putString(KEY_REFRESH, auth.refreshToken)
            .putLong(KEY_USER_ID, auth.user.id)
            .putString(KEY_PHONE, auth.user.phone)
            .putString(KEY_NAME, auth.user.name)
            .putString(KEY_ROLE, auth.user.role)
            .apply()
        _loggedIn.value = true
    }

    fun updateName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().apply()
        _loggedIn.value = false
    }

    private companion object {
        const val KEY_ACCESS = "access"
        const val KEY_REFRESH = "refresh"
        const val KEY_USER_ID = "user_id"
        const val KEY_PHONE = "phone"
        const val KEY_NAME = "name"
        const val KEY_ROLE = "role"
    }
}
