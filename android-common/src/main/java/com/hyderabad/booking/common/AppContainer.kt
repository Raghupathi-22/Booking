package com.hyderabad.booking.common

import android.content.Context
import com.hyderabad.booking.common.network.ApiClient
import com.hyderabad.booking.common.network.BookingApi
import com.hyderabad.booking.common.network.TokenStore

/** Tiny hand-written dependency container (one per app process). */
class AppContainer(context: Context, baseUrl: String, debug: Boolean) {
    val tokenStore = TokenStore(context)
    val api: BookingApi = ApiClient.create(baseUrl, tokenStore, debug)
}
