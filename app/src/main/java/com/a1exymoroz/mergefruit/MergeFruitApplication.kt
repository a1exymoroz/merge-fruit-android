package com.a1exymoroz.mergefruit

import android.app.Application
import com.a1exymoroz.mergefruit.di.AppContainer

class MergeFruitApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
