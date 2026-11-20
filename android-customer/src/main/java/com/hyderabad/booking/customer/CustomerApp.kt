package com.hyderabad.booking.customer

import android.app.Application
import com.hyderabad.booking.common.AppContainer

class CustomerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, BuildConfig.BASE_URL, BuildConfig.DEBUG)
    }
}
