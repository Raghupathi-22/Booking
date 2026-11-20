package com.hyderabad.booking.partner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.hyderabad.booking.common.ui.AppFlavor
import com.hyderabad.booking.common.ui.BookingTheme
import com.hyderabad.booking.partner.ui.PartnerRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PartnerApp).container
        setContent {
            BookingTheme(AppFlavor.PARTNER) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PartnerRoot(container)
                }
            }
        }
    }
}
