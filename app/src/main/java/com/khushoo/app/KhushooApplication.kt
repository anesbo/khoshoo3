package com.khushoo.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.khushoo.app.data.DataStoreManager

class KhushooApplication : Application() {

    lateinit var dataStoreManager: DataStoreManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        dataStoreManager = DataStoreManager(applicationContext)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Active session foreground channel (Low/Default importance to be peaceful)
            val sessionChannel = NotificationChannel(
                CHANNEL_ACTIVE_SESSION,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_desc)
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }

            // Alerts & completed session channel (Completely silent, zero vibration)
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Khushoo Alerts",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent notifications when Khushoo session ends or calls are shielded"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }

            notificationManager.createNotificationChannel(sessionChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    companion object {
        const val CHANNEL_ACTIVE_SESSION = "khushoo_active_session_channel"
        const val CHANNEL_ALERTS = "khushoo_alerts_channel"

        lateinit var instance: KhushooApplication
            private set
    }
}
