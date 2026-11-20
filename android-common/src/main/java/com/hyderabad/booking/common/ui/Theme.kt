package com.hyderabad.booking.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppFlavor { CUSTOMER, PARTNER }

private val CustomerLight = lightColorScheme(
    primary = Color(0xFF6A3DE8), onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF), onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF00897B), tertiary = Color(0xFFE65100),
    background = Color(0xFFFCF8FF), surface = Color(0xFFFCF8FF)
)
private val CustomerDark = darkColorScheme(
    primary = Color(0xFFCFBCFF), onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B), onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFF4DB6AC), tertiary = Color(0xFFFFB74D)
)
private val PartnerLight = lightColorScheme(
    primary = Color(0xFF00796B), onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB), onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF6A3DE8), tertiary = Color(0xFFE65100),
    background = Color(0xFFF7FBFA), surface = Color(0xFFF7FBFA)
)
private val PartnerDark = darkColorScheme(
    primary = Color(0xFF80CBC4), onPrimary = Color(0xFF00352F),
    primaryContainer = Color(0xFF00504A), onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFCFBCFF), tertiary = Color(0xFFFFB74D)
)

@Composable
fun BookingTheme(flavor: AppFlavor, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = when (flavor) {
        AppFlavor.CUSTOMER -> if (dark) CustomerDark else CustomerLight
        AppFlavor.PARTNER -> if (dark) PartnerDark else PartnerLight
    }
    MaterialTheme(colorScheme = colors, content = content)
}
