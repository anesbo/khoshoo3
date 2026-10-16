package com.khushoo.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "khushoo_settings")

data class ShieldedCall(
    val callerNumber: String,
    val timestamp: Long,
    val smsStatus: String
)

class DataStoreManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        val KEY_IS_ACTIVE = booleanPreferencesKey("is_active")
        val KEY_AUTO_REPLY_MESSAGE = stringPreferencesKey("auto_reply_message")
        val KEY_TIMER_HOURS = intPreferencesKey("timer_hours")
        val KEY_TIMER_MINUTES = intPreferencesKey("timer_minutes")
        val KEY_SESSION_END_TIMESTAMP = longPreferencesKey("session_end_timestamp")
        val KEY_SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_SHIELDED_CALLS = stringPreferencesKey("shielded_calls_json")

        // In-memory atomic cache for instant, zero-latency access in CallScreeningService
        private val cachedIsActive = AtomicBoolean(false)
        private val cachedMessage = AtomicReference<String>("")
        private val cachedEndTimestamp = AtomicLong(0L)
    }

    init {
        // Hydrate in-memory cache and keep it synchronized
        scope.launch {
            context.dataStore.data.collect { prefs ->
                val active = prefs[KEY_IS_ACTIVE] ?: false
                val msg = prefs[KEY_AUTO_REPLY_MESSAGE] ?: ""
                val endTs = prefs[KEY_SESSION_END_TIMESTAMP] ?: 0L
                cachedIsActive.set(active)
                cachedMessage.set(msg)
                cachedEndTimestamp.set(endTs)
            }
        }
    }

    // Fast static accessors for Services
    fun isKhushooActiveFast(): Boolean {
        return cachedIsActive.get()
    }

    fun getAutoReplyMessageFast(): String {
        val msg = cachedMessage.get()
        if (msg.isNullOrBlank()) {
            return context.getString(com.khushoo.app.R.string.default_reply_message)
        }
        return msg
    }

    // Flows for Compose UI
    val isActiveFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_ACTIVE] ?: false
    }

    val autoReplyMessageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_REPLY_MESSAGE] ?: context.getString(com.khushoo.app.R.string.default_reply_message)
    }

    val timerHoursFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TIMER_HOURS] ?: 0
    }

    val timerMinutesFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TIMER_MINUTES] ?: 30
    }

    val sessionEndTimestampFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_SESSION_END_TIMESTAMP] ?: 0L
    }

    val selectedLanguageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_LANGUAGE] ?: "ar" // Arabic default per specification
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "light" // Light theme default
    }

    val shieldedCallsFlow: Flow<List<ShieldedCall>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_SHIELDED_CALLS] ?: "[]"
        parseShieldedCalls(jsonStr)
    }

    // Synchronous reads when needed
    fun getIsActiveSync(): Boolean {
        return runBlocking {
            context.dataStore.data.first()[KEY_IS_ACTIVE] ?: false
        }
    }

    fun getSessionEndTimestampSync(): Long {
        return runBlocking {
            context.dataStore.data.first()[KEY_SESSION_END_TIMESTAMP] ?: 0L
        }
    }

    fun getTimerDurationSync(): Pair<Int, Int> {
        return runBlocking {
            val prefs = context.dataStore.data.first()
            val hours = prefs[KEY_TIMER_HOURS] ?: 0
            val minutes = prefs[KEY_TIMER_MINUTES] ?: 30
            Pair(hours, minutes)
        }
    }

    // Updates
    suspend fun setIsActive(active: Boolean) {
        cachedIsActive.set(active)
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_ACTIVE] = active
        }
    }

    suspend fun setAutoReplyMessage(message: String) {
        cachedMessage.set(message)
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_REPLY_MESSAGE] = message
        }
    }

    suspend fun setTimerDuration(hours: Int, minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TIMER_HOURS] = hours
            prefs[KEY_TIMER_MINUTES] = minutes
        }
    }

    suspend fun setSessionEndTimestamp(timestamp: Long) {
        cachedEndTimestamp.set(timestamp)
        context.dataStore.edit { prefs ->
            prefs[KEY_SESSION_END_TIMESTAMP] = timestamp
        }
    }

    suspend fun setSelectedLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_LANGUAGE] = languageCode
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun recordShieldedCall(callerNumber: String, status: String = "SMS Sent") {
        context.dataStore.edit { prefs ->
            val existingJson = prefs[KEY_SHIELDED_CALLS] ?: "[]"
            val list = parseShieldedCalls(existingJson).toMutableList()
            list.add(0, ShieldedCall(callerNumber, System.currentTimeMillis(), status))
            // Keep at most 20 recent calls
            val trimmed = list.take(20)
            prefs[KEY_SHIELDED_CALLS] = serializeShieldedCalls(trimmed)
        }
    }

    suspend fun clearShieldedCalls() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHIELDED_CALLS] = "[]"
        }
    }

    private fun parseShieldedCalls(jsonStr: String): List<ShieldedCall> {
        val list = mutableListOf<ShieldedCall>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ShieldedCall(
                        callerNumber = obj.optString("callerNumber", "Unknown"),
                        timestamp = obj.optLong("timestamp", 0L),
                        smsStatus = obj.optString("smsStatus", "Sent")
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    private fun serializeShieldedCalls(list: List<ShieldedCall>): String {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("callerNumber", item.callerNumber)
            obj.put("timestamp", item.timestamp)
            obj.put("smsStatus", item.smsStatus)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
