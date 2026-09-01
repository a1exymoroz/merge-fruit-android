package com.a1exymoroz.mergefruit.ui.fruit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import com.a1exymoroz.mergefruit.game.FruitType

/**
 * Each fruit is a cute, face-bearing character drawn entirely with a
 * [Canvas] (Compose has no SVG renderer). A soft radial-gradient body, a
 * light glossy highlight, one distinguishing accent per fruit, and a kawaii
 * face (eyes, blush, smile). With [wearHat] the fruit also gets a Santa hat
 * for the New Year skin.
 *
 * Everything is expressed in a 0..100 space and scaled to [sizeDp], so the
 * same drawing works from the 20 dp progress strip up to the 200 dp jar.
 */
@Composable
fun FruitVisual(
    fruitType: FruitType,
    sizeDp: Dp,
    modifier: Modifier = Modifier,
    wearHat: Boolean = false,
) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val s = size.minDimension / 100f
        val center = Offset(50f * s, 52f * s)
        val r = 44f * s
        val palette = paletteFor(fruitType.id)

        drawBody(center, r, palette)
        drawAccent(fruitType.id, center, r, palette)
        drawGloss(center, r)
        drawFace(center, r)
        if (wearHat) drawSantaHat(center, r)
    }
}

// --- palette ------------------------------------------------------------

private data class FruitPalette(val body: Color, val accent: Color)

private fun paletteFor(id: Int): FruitPalette = when (id) {
    1 -> FruitPalette(Color(0xFF5B7BE0), Color(0xFF2E3D8F)) // Blueberry
    2 -> FruitPalette(Color(0xFFE8503C), Color(0xFF3E7D2E)) // Cherry
    3 -> FruitPalette(Color(0xFF9B59B6), Color(0xFF6C3483)) // Plum
    4 -> FruitPalette(Color(0xFFF3CC2E), Color(0xFF6BA03B)) // Lemon
    5 -> FruitPalette(Color(0xFFA1887F), Color(0xFF8CB84E)) // Kiwi
    6 -> FruitPalette(Color(0xFFED8A2B), Color(0xFF3E7D2E)) // Orange
    7 -> FruitPalette(Color(0xFFE84C3D), Color(0xFF4E8A34)) // Apple
    8 -> FruitPalette(Color(0xFFFFA36C), Color(0xFFE0685A)) // Peach
    9 -> FruitPalette(Color(0xFFA1887F), Color(0xFFEFE7DC)) // Coconut
    10 -> FruitPalette(Color(0xFF82C46B), Color(0xFF4F9E3F)) // Melon
    11 -> FruitPalette(Color(0xFF43A047), Color(0xFF2E6B31)) // Watermelon
    else -> FruitPalette(Color(0xFF999999), Color(0xFF666666))
}

// --- pieces -----------------------------------------------------------

private fun DrawScope.drawBody(center: Offset, r: Float, palette: FruitPalette) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.body.lighten(0.35f), palette.body, palette.body.darken(0.22f)),
            center = Offset(center.x - r * 0.35f, center.y - r * 0.35f),
            radius = r * 1.5f,
        ),
        radius = r,
        center = center,
    )
}

private fun DrawScope.drawGloss(center: Offset, r: Float) {
    drawOval(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = Offset(center.x - r * 0.55f, center.y - r * 0.8f),
        size = Size(r * 0.7f, r * 0.5f),
    )
}

/** One recognizable mark per fruit, kept small so the face stays readable. */
private fun DrawScope.drawAccent(id: Int, center: Offset, r: Float, palette: FruitPalette) {
    val cx = center.x
    val cy = center.y
    when (id) {
        1 -> { // blueberry crown
            repeat(5) { i ->
                val a = Math.toRadians((i * 72 - 90).toDouble())
                drawCircle(palette.accent, r * 0.09f, Offset(cx + (r * 0.16f * kotlin.math.cos(a)).toFloat(), cy - r * 0.72f + (r * 0.16f * kotlin.math.sin(a)).toFloat()))
            }
        }
        2 -> { // cherry stem + leaf
            drawLine(Color(0xFF6B4A2B), Offset(cx, cy - r * 0.9f), Offset(cx + r * 0.28f, cy - r * 1.45f), strokeWidth = r * 0.1f, cap = StrokeCap.Round)
            drawOval(palette.accent, Offset(cx + r * 0.22f, cy - r * 1.7f), Size(r * 0.55f, r * 0.32f))
        }
        3 -> drawLine(palette.accent.copy(alpha = 0.5f), Offset(cx, cy - r * 0.9f), Offset(cx, cy + r * 0.9f), strokeWidth = r * 0.08f)
        4 -> { // lemon nub
            drawCircle(palette.body.darken(0.2f), r * 0.12f, Offset(cx + r * 0.85f, cy))
            drawCircle(palette.body.darken(0.2f), r * 0.12f, Offset(cx - r * 0.85f, cy))
        }
        5 -> { // kiwi cut core
            drawCircle(Color(0xFF9CCC65), r * 0.66f, center)
            drawCircle(Color(0xFFF3F0E4), r * 0.24f, center)
            repeat(10) { i ->
                val a = Math.toRadians((i * 36).toDouble())
                drawCircle(Color(0xFF2D3436), r * 0.045f, Offset(cx + (r * 0.42f * kotlin.math.cos(a)).toFloat(), cy + (r * 0.42f * kotlin.math.sin(a)).toFloat()))
            }
        }
        6 -> drawCircle(palette.body.darken(0.25f), r * 0.1f, Offset(cx, cy - r * 0.82f))
        7 -> { // apple stem + leaf
            drawLine(Color(0xFF6B4A2B), Offset(cx, cy - r * 0.85f), Offset(cx, cy - r * 1.3f), strokeWidth = r * 0.12f, cap = StrokeCap.Round)
            drawOval(palette.accent, Offset(cx + r * 0.05f, cy - r * 1.35f), Size(r * 0.6f, r * 0.34f))
        }
        8 -> drawLine(palette.accent.copy(alpha = 0.45f), Offset(cx - r * 0.1f, cy - r * 0.85f), Offset(cx + r * 0.1f, cy + r * 0.85f), strokeWidth = r * 0.09f)
        9 -> { // coconut pale patch
            drawCircle(palette.accent, r * 0.5f, Offset(cx + r * 0.15f, cy + r * 0.1f))
        }
        10 -> { // melon net
            for (k in -2..2) {
                drawLine(palette.accent.copy(alpha = 0.5f), Offset(cx + k * r * 0.32f, cy - r), Offset(cx + k * r * 0.32f, cy + r), strokeWidth = r * 0.05f)
            }
        }
        11 -> { // watermelon stripes
            for (k in -2..2) {
                val x = cx + k * r * 0.42f
                drawLine(palette.accent, Offset(x, cy - r * 0.95f), Offset(x, cy + r * 0.95f), strokeWidth = r * 0.14f)
            }
        }
    }
}

private fun DrawScope.drawFace(center: Offset, r: Float) {
    val cx = center.x
    val cy = center.y
    val eyeDx = r * 0.34f
    val eyeY = cy - r * 0.02f

    listOf(-1f, 1f).forEach { sign ->
        drawCircle(Color(0xFF2B2B2B), r * 0.14f, Offset(cx + sign * eyeDx, eyeY))
        drawCircle(Color.White, r * 0.05f, Offset(cx + sign * eyeDx - r * 0.05f, eyeY - r * 0.05f))
        drawOval(
            color = Color(0xFFFF8FA3).copy(alpha = 0.55f),
            topLeft = Offset(cx + sign * r * 0.62f - r * 0.16f, cy + r * 0.16f),
            size = Size(r * 0.32f, r * 0.2f),
        )
    }

    drawArc(
        color = Color(0xFF2B2B2B),
        startAngle = 25f,
        sweepAngle = 130f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.26f, cy + r * 0.02f),
        size = Size(r * 0.52f, r * 0.4f),
        style = Stroke(width = r * 0.07f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawSantaHat(center: Offset, r: Float) {
    val cx = center.x
    val cy = center.y
    rotate(degrees = -8f, pivot = center) {
        val cone = Path().apply {
            moveTo(cx - r * 0.72f, cy - r * 0.52f)
            quadraticBezierTo(cx - r * 0.15f, cy - r * 1.55f, cx + r * 0.6f, cy - r * 0.98f)
            lineTo(cx + r * 0.64f, cy - r * 0.34f)
            quadraticBezierTo(cx - r * 0.05f, cy - r * 0.72f, cx - r * 0.72f, cy - r * 0.52f)
            close()
        }
        drawPath(cone, Color(0xFFD32F2F))
        drawLine(
            Color.White,
            Offset(cx - r * 0.8f, cy - r * 0.46f),
            Offset(cx + r * 0.66f, cy - r * 0.3f),
            strokeWidth = r * 0.26f,
            cap = StrokeCap.Round,
        )
        drawCircle(Color.White, r * 0.17f, Offset(cx + r * 0.6f, cy - r * 1.02f))
    }
}

// --- colour helpers ---------------------------------------------------

private fun Color.lighten(t: Float): Color = mix(Color.White, t)
private fun Color.darken(t: Float): Color = mix(Color.Black, t)
private fun Color.mix(other: Color, t: Float): Color = Color(
    red = red * (1 - t) + other.red * t,
    green = green * (1 - t) + other.green * t,
    blue = blue * (1 - t) + other.blue * t,
    alpha = alpha,
)
