package com.khushoo.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khushoo.app.R
import com.khushoo.app.data.ShieldedCall
import com.khushoo.app.ui.components.AppFooter
import com.khushoo.app.ui.components.AutoReplyMessageCard
import com.khushoo.app.ui.components.KhushooActionButton
import com.khushoo.app.ui.components.LanguageSelector
import com.khushoo.app.ui.components.PermissionCard
import com.khushoo.app.ui.components.ShieldedCallsCard
import com.khushoo.app.ui.components.TimerDurationCard
import com.khushoo.app.ui.theme.ActiveRose
import com.khushoo.app.ui.theme.PureWhite
import com.khushoo.app.ui.theme.SurfaceLight
import com.khushoo.app.ui.theme.TextSecondaryDark
import com.khushoo.app.ui.theme.TextSecondaryLight
import com.khushoo.app.ui.theme.TrueBlack

@Composable
fun MainScreen(
    isActive: Boolean,
    isDarkTheme: Boolean,
    autoReplyMessage: String,
    hours: Int,
    minutes: Int,
    sessionEndTimestamp: Long,
    selectedLanguage: String,
    shieldedCalls: List<ShieldedCall>,
    onMessageChange: (String) -> Unit,
    onDurationChange: (hours: Int, minutes: Int) -> Unit,
    onToggleKhushoo: () -> Unit,
    onToggleTheme: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onClearShieldedCalls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Minimalist header background gradient based on theme and active state
    val headerGradient = Brush.verticalGradient(
        colors = when {
            isActive && isDarkTheme -> listOf(Color(0xFF2B0A12), TrueBlack, TrueBlack)
            isActive && !isDarkTheme -> listOf(Color(0xFFFFEEF0), SurfaceLight, SurfaceLight)
            isDarkTheme -> listOf(TrueBlack, TrueBlack)
            else -> listOf(SurfaceLight, SurfaceLight)
        }
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(headerGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Bar with Branding, Theme Toggle, and Language Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDarkTheme) Color(0xFF1C1C1C) else Color(0xFFEFEFEF)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = if (isActive) ActiveRose else if (isDarkTheme) PureWhite else TrueBlack,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) PureWhite else TrueBlack,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = stringResource(R.string.tagline),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Light / Dark Theme Toggle
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) Color(0xFF1C1C1C) else Color(0xFFEFEFEF))
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (isDarkTheme) PureWhite else TrueBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Multi-language switcher
                        LanguageSelector(
                            currentLanguageCode = selectedLanguage,
                            onLanguageSelected = onLanguageChange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Permissions banner if any permission is missing
                PermissionCard()

                Spacer(modifier = Modifier.height(14.dp))

                // ========================================================
                // 1. VERY TOP: TIMER DURATION SELECTOR (Hours & Minutes)
                // ========================================================
                TimerDurationCard(
                    hours = hours,
                    minutes = minutes,
                    onDurationChange = onDurationChange
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ========================================================
                // 2. IN THE MIDDLE: MAIN ACTION BUTTON (Activation Toggle)
                // ========================================================
                KhushooActionButton(
                    isActive = isActive,
                    sessionEndTimestamp = sessionEndTimestamp,
                    onToggle = onToggleKhushoo
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ========================================================
                // 3. AT THE VERY BOTTOM: AUTO-REPLY MESSAGE INPUT BOX
                // ========================================================
                AutoReplyMessageCard(
                    message = autoReplyMessage,
                    onMessageChange = onMessageChange
                )

                // Shielded calls drawer / list
                if (shieldedCalls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    ShieldedCallsCard(
                        calls = shieldedCalls,
                        onClear = onClearShieldedCalls
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ========================================================
                // 4. COPYRIGHT & ATTRIBUTION FOOTER
                // ========================================================
                AppFooter()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
