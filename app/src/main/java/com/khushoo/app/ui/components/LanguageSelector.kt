package com.khushoo.app.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.khushoo.app.ui.theme.PearlBlueAccent
import com.khushoo.app.ui.theme.PearlNavyDark
import com.khushoo.app.ui.theme.PureWhite

data class LanguageItem(
    val code: String,
    val displayName: String
)

@Composable
fun LanguageSelector(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf(
        LanguageItem("ar", "العربية"),
        LanguageItem("fr", "Français"),
        LanguageItem("en", "English")
    )

    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            languages.forEach { lang ->
                val isSelected = currentLanguageCode.startsWith(lang.code)

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "langBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "langText"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(bgColor)
                        .clickable {
                            if (!isSelected) {
                                // Apply locale application-wide via AppCompatDelegate
                                val appLocale = LocaleListCompat.forLanguageTags(lang.code)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                                onLanguageSelected(lang.code)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lang.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
