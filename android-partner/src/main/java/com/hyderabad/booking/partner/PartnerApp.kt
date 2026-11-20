package com.hyderabad.booking.partner

import android.app.Application
import com.hyderabad.booking.common.AppContainer

class PartnerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, BuildConfig.BASE_URL, BuildConfig.DEBUG)
    }
}
