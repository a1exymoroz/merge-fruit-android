package com.a1exymoroz.mergefruit.ui.game

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random

/** Full-screen drifting snow, drawn behind everything on the game screen when the New Year skin is on. */
@Composable
fun SnowLayer(modifier: Modifier = Modifier) {
    val flakes = remember {
        val rng = Random(42)
        List(46) {
            Flake(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                radius = 1.5f + rng.nextFloat() * 3.5f,
                speed = 0.02f + rng.nextFloat() * 0.06f,
                drift = (rng.nextFloat() - 0.5f) * 0.04f,
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "snow")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "snow-progress",
    )

    Canvas(modifier = modifier) {
        flakes.forEach { f ->
            val y = ((f.y + t * f.speed * 30f) % 1f) * size.height
            val x = ((f.x + t * f.drift * 30f) % 1f) * size.width
            drawCircle(Color.White.copy(alpha = 0.85f), f.radius, Offset(x, y))
        }
    }
}

private data class Flake(val x: Float, val y: Float, val radius: Float, val speed: Float, val drift: Float)

/** A striped candy cane, drawn above the jar for the New Year skin. */
@Composable
fun CandyCane(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val thickness = h * 0.26f
        // A cane lying on its side: a short hook that curls up and over, flowing into a long shaft.
        val path = Path().apply {
            moveTo(w * 0.14f, h * 0.62f)
            quadraticBezierTo(w * 0.10f, h * 0.16f, w * 0.30f, h * 0.16f)
            quadraticBezierTo(w * 0.44f, h * 0.16f, w * 0.46f, h * 0.46f)
            moveTo(w * 0.30f, h * 0.16f)
            quadraticBezierTo(w * 0.62f, h * 0.20f, w * 0.90f, h * 0.78f)
        }
        // Solid red cane, then white barber-pole stripes on top via a dashed stroke.
        drawPath(path, Color(0xFFE23B3B), style = Stroke(width = thickness, cap = StrokeCap.Round))
        drawPath(
            path,
            Color.White,
            style = Stroke(
                width = thickness,
                cap = StrokeCap.Butt,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(thickness * 0.5f, thickness * 0.8f), 0f),
            ),
        )
    }
}
