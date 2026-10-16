package com.khushoo.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.database.Cursor
import android.os.Build
import android.os.IBinder
import android.provider.CallLog
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.khushoo.app.KhushooApplication
import com.khushoo.app.R
import com.khushoo.app.data.DataStoreManager
import com.khushoo.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class KhushooForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var tickerJob: Job? = null
    private var phoneStateReceiver: BroadcastReceiver? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_DEACTIVATE = "com.khushoo.app.ACTION_NOTIFICATION_DEACTIVATE"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DEACTIVATE || intent?.action == TimerManager.ACTION_STOP_KHUSHOO) {
            TimerManager.deactivateKhushoo(this, "Notification Action")
            stopSelf()
            return START_NOT_STICKY
        }

        // Start Foreground immediately
        val initialNotification = buildNotification(getString(R.string.status_active))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        registerRealtimeCallInterceptor()
        startTicker()
        return START_STICKY
    }

    private fun registerRealtimeCallInterceptor() {
        if (phoneStateReceiver == null) {
            phoneStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                            var incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                            CallInterceptorHelper.handleIncomingCall(context, incomingNumber)

                            if (incomingNumber.isNullOrBlank()) {
                                serviceScope.launch(Dispatchers.IO) {
                                    delay(350L)
                                    val number = getLatestCallNumber(context)
                                    if (!number.isNullOrBlank()) {
                                        CallInterceptorHelper.handleIncomingCall(context, number)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            registerReceiver(phoneStateReceiver, filter)
        }
    }

    private fun getLatestCallNumber(context: Context): String? {
        return try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER),
                "${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.INCOMING_TYPE.toString(), CallLog.Calls.MISSED_TYPE.toString()),
                "${CallLog.Calls.DATE} DESC LIMIT 1"
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(CallLog.Calls.NUMBER)
                    if (index != -1) it.getString(index) else null
                } else null
            }
        } catch (_: Exception) { null }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        val dataStoreManager = (applicationContext as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(applicationContext)

        tickerJob = serviceScope.launch {
            while (isActive) {
                val endTimestamp = dataStoreManager.getSessionEndTimestampSync()
                val isStillActive = dataStoreManager.getIsActiveSync()

                if (!isStillActive) {
                    stopSelf()
                    break
                }

                if (endTimestamp > 0L) {
                    val remainingSeconds = ((endTimestamp - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                    if (remainingSeconds <= 0) {
                        TimerManager.deactivateKhushoo(applicationContext, "Timer Expired")
                        stopSelf()
                        break
                    }
                    val mins = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                    val contentText = getString(R.string.notif_body_countdown, timeStr)
                    updateNotification(contentText)
                } else {
                    updateNotification(getString(R.string.notif_body_manual))
                }

                TimerManager.updateQuickSettingsTile(applicationContext)
                delay(1000L)
            }
        }
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? android.app.NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildNotification(contentText))
    }

    private fun buildNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deactivateIntent = Intent(this, KhushooForegroundService::class.java).apply {
            action = ACTION_DEACTIVATE
        }
        val deactivatePendingIntent = PendingIntent.getService(
            this,
            1,
            deactivateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, KhushooApplication.CHANNEL_ACTIVE_SESSION)
            .setSmallIcon(R.drawable.ic_stat_khushoo)
            .setContentTitle(getString(R.string.notif_title_active))
            .setContentText(contentText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_stat_khushoo,
                getString(R.string.notif_action_deactivate),
                deactivatePendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        phoneStateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) { }
            phoneStateReceiver = null
        }
        tickerJob?.cancel()
        super.onDestroy()
    }
}
