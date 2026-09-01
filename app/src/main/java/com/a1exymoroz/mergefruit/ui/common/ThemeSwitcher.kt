package com.a1exymoroz.mergefruit.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.ui.theme.GameThemeOption

/**
 * HUD toggle between the classic and New Year skins. The chosen skin is
 * persisted (see [com.a1exymoroz.mergefruit.data.settings.SettingsStorage]);
 * this only reflects [current] and reports taps via [onToggle].
 */
@Composable
fun ThemeSwitcher(
    current: GameThemeOption,
    onToggle: (GameThemeOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val winter = current == GameThemeOption.WINTER
    val next = if (winter) GameThemeOption.CLASSIC else GameThemeOption.WINTER

    OutlinedButton(
        onClick = { onToggle(next) },
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = if (winter) Color.White.copy(alpha = 0.20f) else Color.Transparent,
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(if (winter) "❄️" else "🎄", fontSize = 14.sp)
        Text(
            stringResource(if (winter) R.string.theme_winter else R.string.theme_classic),
            modifier = Modifier.padding(start = 6.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
    }
}
