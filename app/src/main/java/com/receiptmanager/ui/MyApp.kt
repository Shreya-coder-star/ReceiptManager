package com.receiptmanager

import android.app.Application
import com.receiptmanager.util.ThemeManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySavedTheme(this)
    }
}