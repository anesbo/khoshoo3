package com.khushoo.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CallLog
import android.telephony.TelephonyManager
import com.khushoo.app.service.CallInterceptorHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KhushooCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            return
        }

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            var incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            // Instantly trigger silence and hangup
            CallInterceptorHelper.handleIncomingCall(context, incomingNumber)

            // If number was null or blank due to Android 10+ privacy restrictions,
            // query the recent call log after 300ms to retrieve the exact caller number and send SMS
            if (incomingNumber.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    delay(350L)
                    val retrievedNumber = getLatestIncomingCallerNumber(context)
                    if (!retrievedNumber.isNullOrBlank()) {
                        CallInterceptorHelper.handleIncomingCall(context, retrievedNumber)
                    }
                }
            }
        }
    }

    private fun getLatestIncomingCallerNumber(context: Context): String? {
        return try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE),
                "${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.INCOMING_TYPE.toString(), CallLog.Calls.MISSED_TYPE.toString()),
                "${CallLog.Calls.DATE} DESC LIMIT 1"
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                    if (numberIndex != -1) {
                        return it.getString(numberIndex)
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
