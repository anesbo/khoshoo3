package com.khushoo.app.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.khushoo.app.KhushooApplication
import com.khushoo.app.data.DataStoreManager

class KhushooCallService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val appContext = applicationContext
        val dataStoreManager = (appContext as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(appContext)

        // Only screen incoming calls
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            return
        }

        val isActive = dataStoreManager.isKhushooActiveFast()
        val endTimestamp = dataStoreManager.getSessionEndTimestampSync()

        // Check if session has expired naturally
        if (isActive && endTimestamp > 0L && System.currentTimeMillis() >= endTimestamp) {
            TimerManager.deactivateKhushoo(appContext, "Timer Expired")
            return
        }

        if (!isActive) {
            return
        }

        // 1. Silent rejection response for Telecom
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false) // Retain in call history
            .setSkipNotification(true) // Silence phone ringing and heads-up alert
            .build()

        respondToCall(callDetails, response)

        // 2. Extract caller phone number and trigger automated SMS reply
        val handle = callDetails.handle
        val callerNumber = handle?.schemeSpecificPart?.trim()

        CallInterceptorHelper.handleIncomingCall(appContext, callerNumber)
    }
}
