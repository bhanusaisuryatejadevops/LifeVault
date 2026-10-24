package com.example

import android.app.Application
import com.example.data.VaultDatabase
import com.example.data.VaultRepository
import com.example.monetization.AdMobManager
import com.example.notification.NotificationHelper

class LifeVaultApplication : Application() {

    val database by lazy { VaultDatabase.getDatabase(this) }
    val repository by lazy { VaultRepository(database.vaultDao()) }

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize notification channels for warranty & deadline alerts
        NotificationHelper.createNotificationChannel(this)

        // 2. Initialize AdMob with official Google Mobile Ads SDK
        AdMobManager.initialize(this)
    }
}
