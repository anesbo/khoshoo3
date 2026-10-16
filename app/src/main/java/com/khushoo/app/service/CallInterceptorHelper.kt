package com.khushoo.app.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.SmsManager
import com.khushoo.app.KhushooApplication
import com.khushoo.app.data.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object CallInterceptorHelper {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val lastSmsTimestampByNumber = ConcurrentHashMap<String, Long>()
    private const val SMS_COOLDOWN_MILLIS = 2 * 60 * 1000L // 2 minutes cooldown per caller

    fun handleIncomingCall(context: Context, rawNumber: String?) {
        val appContext = context.applicationContext
        val dataStoreManager = (appContext as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(appContext)

        val isActive = dataStoreManager.isKhushooActiveFast()
        val endTimestamp = dataStoreManager.getSessionEndTimestampSync()

        if (!isActive) return

        // Check if session has expired
        if (endTimestamp > 0L && System.currentTimeMillis() >= endTimestamp) {
            TimerManager.deactivateKhushoo(appContext, "Timer Expired")
            return
        }

        // 1. Immediately silence ringer & notification volume to prevent any sound
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

        val telecomManager = appContext.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        try {
            telecomManager?.silenceRinger()
        } catch (_: Exception) { }

        // 2. Immediately hang up / end incoming call
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                @Suppress("MissingPermission")
                telecomManager?.endCall()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Automated SMS reply without emitting any audible notification
        val cleanNumber = rawNumber?.trim()
        if (!cleanNumber.isNullOrBlank() && !isRestrictedOrAnonymous(cleanNumber)) {
            val now = System.currentTimeMillis()
            val lastSent = lastSmsTimestampByNumber[cleanNumber] ?: 0L

            if (now - lastSent > SMS_COOLDOWN_MILLIS) {
                lastSmsTimestampByNumber[cleanNumber] = now
                val message = dataStoreManager.getAutoReplyMessageFast()

                scope.launch {
                    val sent = sendSms(appContext, cleanNumber, message)
                    dataStoreManager.recordShieldedCall(
                        callerNumber = cleanNumber,
                        status = if (sent) "SMS Sent" else "SMS Failed"
                    )
                }
            } else {
                scope.launch {
                    dataStoreManager.recordShieldedCall(
                        callerNumber = cleanNumber,
                        status = "Shielded (SMS cooldown)"
                    )
                }
            }
        }
    }

    private fun isRestrictedOrAnonymous(number: String): Boolean {
        val lower = number.lowercase()
        return lower.contains("private") || lower.contains("unknown") || lower.contains("restricted") || number.length < 3
    }

    private fun sendSms(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
