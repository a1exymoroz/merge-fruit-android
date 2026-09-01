package com.a1exymoroz.mergefruit.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

private val MergeFruitColorScheme = darkColorScheme(
    primary = GradientStart,
    secondary = GradientEnd,
    background = GradientStart,
    surface = CardBackground,
    error = ErrorRed,
)

/** The full-screen diagonal gradient background used on every screen, ported from body { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) }. */
fun Modifier.appBackground(): Modifier = this.background(
    Brush.linearGradient(
        colors = listOf(GradientStart, GradientEnd),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f),
    ),
)

@Composable
fun MergeFruitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MergeFruitColorScheme,
        typography = Typography(),
        content = content,
    )
}
