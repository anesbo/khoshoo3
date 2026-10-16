package com.khushoo.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShieldMoon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khushoo.app.R
import com.khushoo.app.ui.theme.ActiveGradientEnd
import com.khushoo.app.ui.theme.ActiveGradientStart
import com.khushoo.app.ui.theme.ActiveRose
import com.khushoo.app.ui.theme.ActiveRoseGlow
import com.khushoo.app.ui.theme.PearlBlueAccent
import com.khushoo.app.ui.theme.PearlGradientEnd
import com.khushoo.app.ui.theme.PearlGradientStart
import com.khushoo.app.ui.theme.PureWhite
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun KhushooActionButton(
    isActive: Boolean,
    sessionEndTimestamp: Long,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Live remaining countdown tracker
    var remainingSeconds by remember(sessionEndTimestamp, isActive) {
        mutableLongStateOf(
            if (isActive && sessionEndTimestamp > 0L) {
                ((sessionEndTimestamp - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            } else 0L
        )
    }

    LaunchedEffect(isActive, sessionEndTimestamp) {
        if (isActive && sessionEndTimestamp > 0L) {
            while (true) {
                val rem = ((sessionEndTimestamp - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                remainingSeconds = rem
                if (rem <= 0) break
                delay(1000L)
            }
        }
    }

    val gradientBrush = if (isActive) {
        Brush.radialGradient(
            colors = listOf(ActiveGradientEnd, ActiveGradientStart),
            radius = 240f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(PearlGradientEnd, PearlGradientStart),
            radius = 260f
        )
    }

    val glowColor by animateColorAsState(
        targetValue = if (isActive) ActiveRoseGlow else PearlBlueAccent.copy(alpha = 0.25f),
        label = "glowColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Outer pulsing ring when active
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(190.dp)
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(185.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(ActiveRose.copy(alpha = pulseAlpha))
                )
            }

            // Glow shadow ring
            Box(
                modifier = Modifier
                    .size(165.dp)
                    .clip(CircleShape)
                    .background(glowColor)
            )

            // Main Circular Action Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(150.dp)
                    .shadow(12.dp, CircleShape, spotColor = if (isActive) ActiveRose else PearlBlueAccent)
                    .clip(CircleShape)
                    .background(gradientBrush)
                    .border(
                        width = 2.dp,
                        color = if (isActive) ActiveRose else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onToggle()
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Rounded.ShieldMoon else Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(38.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isActive) {
                            stringResource(R.string.btn_deactivate)
                        } else {
                            stringResource(R.string.btn_activate)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        fontSize = 15.sp
                    )

                    // Live countdown under text when active
                    if (isActive) {
                        Spacer(modifier = Modifier.height(2.dp))
                        if (sessionEndTimestamp > 0L) {
                            val mins = remainingSeconds / 60
                            val secs = remainingSeconds % 60
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFE4E6),
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.manual_mode),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFE4E6),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status description pill below button
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isActive) ActiveRose.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isActive) ActiveRose.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) ActiveRose else Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) {
                            if (sessionEndTimestamp > 0L) {
                                val mins = remainingSeconds / 60
                                val secs = remainingSeconds % 60
                                val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                                stringResource(R.string.remaining_time, timeFormatted)
                            } else {
                                stringResource(R.string.manual_active_desc)
                            }
                        } else {
                            stringResource(R.string.status_inactive)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) ActiveRose else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
