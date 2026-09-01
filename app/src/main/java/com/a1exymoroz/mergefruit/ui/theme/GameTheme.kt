package com.a1exymoroz.mergefruit.ui.theme

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** The two selectable skins. Persisted by [com.a1exymoroz.mergefruit.data.settings.SettingsStorage]. */
enum class GameThemeOption { CLASSIC, WINTER }

/**
 * The current skin, provided at the app root from the persisted setting.
 * Defaults to [GameThemeOption.CLASSIC] so previews and the auth screens have a value.
 */
val LocalGameTheme = staticCompositionLocalOf { GameThemeOption.CLASSIC }

/** Resolved colours / flags for one skin — everything the game screen needs to paint itself. */
data class GameThemeSpec(
    val option: GameThemeOption,
    val containerBackground: Color,
    val containerBorder: Color,
    val hudCardBackground: Color,
    val wearHats: Boolean,
    val showSnow: Boolean,
    val showCandyCane: Boolean,
)

fun gameThemeSpec(option: GameThemeOption): GameThemeSpec = when (option) {
    GameThemeOption.CLASSIC -> GameThemeSpec(
        option = option,
        containerBackground = ContainerBackground,
        containerBorder = Color.White.copy(alpha = 0.25f),
        hudCardBackground = Color.White.copy(alpha = 0.15f),
        wearHats = false,
        showSnow = false,
        showCandyCane = false,
    )
    GameThemeOption.WINTER -> GameThemeSpec(
        option = option,
        containerBackground = WinterContainerBackground,
        containerBorder = WinterContainerBorder,
        hudCardBackground = Color.White.copy(alpha = 0.30f),
        wearHats = true,
        showSnow = true,
        showCandyCane = true,
    )
}

/** Full-screen background for the game screen, swapped per skin. */
fun Modifier.gameBackground(option: GameThemeOption): Modifier {
    val colors = when (option) {
        GameThemeOption.CLASSIC -> listOf(GradientStart, GradientEnd)
        GameThemeOption.WINTER -> listOf(WinterGradientStart, WinterGradientEnd)
    }
    return this.background(
        Brush.linearGradient(colors = colors, start = Offset(0f, 0f), end = Offset(0f, 2000f)),
    )
}

@Composable
fun rememberGameThemeSpec(option: GameThemeOption): GameThemeSpec = gameThemeSpec(option)
