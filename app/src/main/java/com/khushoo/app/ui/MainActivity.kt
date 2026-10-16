package com.khushoo.app.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.os.LocaleListCompat
import com.khushoo.app.KhushooApplication
import com.khushoo.app.R
import com.khushoo.app.data.DataStoreManager
import com.khushoo.app.service.TimerManager
import com.khushoo.app.ui.theme.KhushooTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreManager = (application as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(applicationContext)

        setContent {
            val coroutineScope = rememberCoroutineScope()

            val themeMode by dataStoreManager.themeModeFlow.collectAsState(initial = "light")
            val isDark = themeMode == "dark"

            KhushooTheme(darkTheme = isDark) {
                val isActive by dataStoreManager.isActiveFlow.collectAsState(initial = false)
                val autoReplyMessage by dataStoreManager.autoReplyMessageFlow.collectAsState(
                    initial = getString(R.string.default_reply_message)
                )
                val hours by dataStoreManager.timerHoursFlow.collectAsState(initial = 0)
                val minutes by dataStoreManager.timerMinutesFlow.collectAsState(initial = 30)
                val sessionEndTimestamp by dataStoreManager.sessionEndTimestampFlow.collectAsState(initial = 0L)
                val selectedLanguage by dataStoreManager.selectedLanguageFlow.collectAsState(initial = "ar")
                val shieldedCalls by dataStoreManager.shieldedCallsFlow.collectAsState(initial = emptyList())

                MainScreen(
                    isActive = isActive,
                    isDarkTheme = isDark,
                    autoReplyMessage = autoReplyMessage,
                    hours = hours,
                    minutes = minutes,
                    sessionEndTimestamp = sessionEndTimestamp,
                    selectedLanguage = selectedLanguage,
                    shieldedCalls = shieldedCalls,
                    onMessageChange = { newMsg ->
                        coroutineScope.launch {
                            dataStoreManager.setAutoReplyMessage(newMsg)
                        }
                    },
                    onDurationChange = { newH, newM ->
                        coroutineScope.launch {
                            dataStoreManager.setTimerDuration(newH, newM)
                        }
                    },
                    onToggleKhushoo = {
                        if (isActive) {
                            TimerManager.deactivateKhushoo(this@MainActivity, "User Toggle")
                        } else {
                            val totalMinutes = (hours * 60) + minutes
                            TimerManager.activateKhushoo(
                                context = this@MainActivity,
                                durationMinutes = totalMinutes,
                                customMessage = autoReplyMessage
                            )
                        }
                    },
                    onToggleTheme = {
                        val nextMode = if (isDark) "light" else "dark"
                        coroutineScope.launch {
                            dataStoreManager.setThemeMode(nextMode)
                        }
                    },
                    onLanguageChange = { langCode ->
                        coroutineScope.launch {
                            dataStoreManager.setSelectedLanguage(langCode)
                            val appLocale = LocaleListCompat.forLanguageTags(langCode)
                            AppCompatDelegate.setApplicationLocales(appLocale)
                        }
                    },
                    onClearShieldedCalls = {
                        coroutineScope.launch {
                            dataStoreManager.clearShieldedCalls()
                        }
                    }
                )
            }
        }
    }
}
