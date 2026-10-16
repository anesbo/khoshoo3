package com.khushoo.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khushoo.app.R
import com.khushoo.app.ui.theme.PearlBlueAccent
import com.khushoo.app.ui.theme.PearlBluePrimary
import com.khushoo.app.ui.theme.PureWhite
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerDurationCard(
    hours: Int,
    minutes: Int,
    onDurationChange: (hours: Int, minutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isManualMode = hours == 0 && minutes == 0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.section_timer),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isManualMode) {
                            stringResource(R.string.manual_mode)
                        } else {
                            String.format(
                                Locale.getDefault(),
                                "%02d %s : %02d %s",
                                hours,
                                stringResource(R.string.hours_short),
                                minutes,
                                stringResource(R.string.minutes_short)
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isManualMode) PearlBlueAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isManualMode) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hours & Minutes Interactive Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hours Unit
                TimeUnitPicker(
                    value = hours,
                    label = stringResource(R.string.hours_label),
                    onIncrement = {
                        val newH = (hours + 1).coerceAtMost(23)
                        onDurationChange(newH, minutes)
                    },
                    onDecrement = {
                        val newH = (hours - 1).coerceAtLeast(0)
                        onDurationChange(newH, minutes)
                    }
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Minutes Unit (steps of 5 or 1)
                TimeUnitPicker(
                    value = minutes,
                    label = stringResource(R.string.minutes_label),
                    onIncrement = {
                        val newM = if (minutes >= 55) 0 else ((minutes / 5) + 1) * 5
                        val newH = if (minutes >= 55) (hours + 1).coerceAtMost(23) else hours
                        onDurationChange(newH, newM)
                    },
                    onDecrement = {
                        val newM = if (minutes <= 0) {
                            if (hours > 0) 55 else 0
                        } else {
                            (((minutes - 1) / 5) * 5).coerceAtLeast(0)
                        }
                        val newH = if (minutes <= 0 && hours > 0) hours - 1 else hours
                        onDurationChange(newH, newM)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Preset Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickDurationChip(
                    label = stringResource(R.string.chip_15m),
                    isSelected = hours == 0 && minutes == 15,
                    onClick = { onDurationChange(0, 15) }
                )
                QuickDurationChip(
                    label = stringResource(R.string.chip_30m),
                    isSelected = hours == 0 && minutes == 30,
                    onClick = { onDurationChange(0, 30) }
                )
                QuickDurationChip(
                    label = stringResource(R.string.chip_45m),
                    isSelected = hours == 0 && minutes == 45,
                    onClick = { onDurationChange(0, 45) }
                )
                QuickDurationChip(
                    label = stringResource(R.string.chip_1h),
                    isSelected = hours == 1 && minutes == 0,
                    onClick = { onDurationChange(1, 0) }
                )
                QuickDurationChip(
                    label = stringResource(R.string.chip_manual),
                    isSelected = isManualMode,
                    onClick = { onDurationChange(0, 0) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Helper notice
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = PearlBlueAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.timer_helper_manual),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun TimeUnitPicker(
    value: Int,
    label: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Increment Button
        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increase $label",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Large Number Display
        Surface(
            modifier = Modifier
                .width(84.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%02d", value),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Decrement Button
        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Decrease $label",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun QuickDurationChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        label = "chipBg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
