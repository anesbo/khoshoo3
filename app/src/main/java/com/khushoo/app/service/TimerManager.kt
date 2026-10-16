package com.khushoo.app.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.service.quicksettings.TileService
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.khushoo.app.KhushooApplication
import com.khushoo.app.R
import com.khushoo.app.data.DataStoreManager
import com.khushoo.app.receiver.KhushooAlarmReceiver
import com.khushoo.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TimerManager {

    const val ACTION_START_KHUSHOO = "com.khushoo.app.ACTION_START_KHUSHOO"
    const val ACTION_STOP_KHUSHOO = "com.khushoo.app.ACTION_STOP_KHUSHOO"
    const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"

    private val scope = CoroutineScope(Dispatchers.IO)

    fun activateKhushoo(
        context: Context,
        durationMinutes: Int,
        customMessage: String? = null
    ) {
        val appContext = context.applicationContext
        val dataStoreManager = (appContext as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(appContext)

        val endTimestamp = if (durationMinutes > 0) {
            System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        } else {
            0L // 0 indicates Manual Mode
        }

        scope.launch {
            dataStoreManager.setIsActive(true)
            dataStoreManager.setSessionEndTimestamp(endTimestamp)
            if (!customMessage.isNullOrBlank()) {
                dataStoreManager.setAutoReplyMessage(customMessage)
            }

            // Schedule Alarm if not manual
            if (endTimestamp > 0L) {
                scheduleAlarm(appContext, endTimestamp)
            } else {
                cancelAlarm(appContext)
            }

            // Mute notifications & ringer for peaceful silence
            try {
                val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.ringerMode = AudioManager.RINGER_MODE_SILENT
                audioManager?.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
                audioManager?.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                audioManager?.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
            } catch (_: Exception) { }

            // Set DND if access granted
            try {
                val notifManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notifManager?.isNotificationPolicyAccessGranted == true) {
                    notifManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                }
            } catch (_: Exception) { }

            // Start Foreground Service
            val serviceIntent = Intent(appContext, KhushooForegroundService::class.java).apply {
                action = ACTION_START_KHUSHOO
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
            }
            try {
                ContextCompat.startForegroundService(appContext, serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Request Tile update
            updateQuickSettingsTile(appContext)
        }
    }

    fun deactivateKhushoo(context: Context, reason: String = "User Manual") {
        val appContext = context.applicationContext
        val dataStoreManager = (appContext as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(appContext)

        scope.launch {
            dataStoreManager.setIsActive(false)
            dataStoreManager.setSessionEndTimestamp(0L)

            // Cancel any scheduled alarm
            cancelAlarm(appContext)

            // Stop Foreground Service
            val serviceIntent = Intent(appContext, KhushooForegroundService::class.java).apply {
                action = ACTION_STOP_KHUSHOO
            }
            try {
                appContext.stopService(serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Restore normal audio mode
            try {
                val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.ringerMode = AudioManager.RINGER_MODE_NORMAL
            } catch (_: Exception) { }

            // Restore normal notification filter
            try {
                val notifManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notifManager?.isNotificationPolicyAccessGranted == true) {
                    notifManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            } catch (_: Exception) { }

            // Request Tile update
            updateQuickSettingsTile(appContext)

            // If deactivated because timer expired, show silent completion notification
            if (reason == "Timer Expired") {
                showSessionEndedNotification(appContext)
            }
        }
    }

    private fun scheduleAlarm(context: Context, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, KhushooAlarmReceiver::class.java).apply {
            action = KhushooAlarmReceiver.ACTION_AUTO_DEACTIVATE
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // In Android 12+, exact alarm permission might not be granted; fall back to normal set
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, KhushooAlarmReceiver::class.java).apply {
            action = KhushooAlarmReceiver.ACTION_AUTO_DEACTIVATE
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, flags)
        alarmManager.cancel(pendingIntent)
    }

    fun updateQuickSettingsTile(context: Context) {
        try {
            TileService.requestListeningState(
                context,
                ComponentName(context, KhushooTileService::class.java)
            )
        } catch (_: Exception) { }
    }

    private fun showSessionEndedNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, KhushooApplication.CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_khushoo)
            .setContentTitle(context.getString(R.string.notif_session_ended_title))
            .setContentText(context.getString(R.string.notif_session_ended_body))
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(2002, notification)
    }
}
