package com.khushoo.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.khushoo.app.KhushooApplication
import com.khushoo.app.data.DataStoreManager
import com.khushoo.app.service.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KhushooAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_AUTO_DEACTIVATE = "com.khushoo.app.ACTION_AUTO_DEACTIVATE"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val dataStoreManager = (appContext as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(appContext)

        when (intent?.action) {
            ACTION_AUTO_DEACTIVATE -> {
                TimerManager.deactivateKhushoo(appContext, "Timer Expired")
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val isActive = dataStoreManager.getIsActiveSync()
                    val endTimestamp = dataStoreManager.getSessionEndTimestampSync()

                    if (isActive) {
                        if (endTimestamp > 0L) {
                            val remaining = endTimestamp - System.currentTimeMillis()
                            if (remaining > 0) {
                                val remainingMinutes = (remaining / 60000).toInt().coerceAtLeast(1)
                                TimerManager.activateKhushoo(appContext, remainingMinutes)
                            } else {
                                TimerManager.deactivateKhushoo(appContext, "Timer Expired")
                            }
                        } else {
                            // Manual mode
                            TimerManager.activateKhushoo(appContext, 0)
                        }
                    }
                }
            }
        }
    }
}
