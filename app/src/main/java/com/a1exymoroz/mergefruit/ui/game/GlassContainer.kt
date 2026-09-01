package com.a1exymoroz.mergefruit.ui.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

/**
 * The play area drawn as an open-topped glass box seen slightly from
 * above-left: a front face (the rectangle the fruits actually live in),
 * a receding top face that reads as the box's mouth, and a right side
 * wall. [drawGlassBoxBack] paints everything behind the fruits;
 * [drawGlassBoxFront] paints the near edges and reflections over them.
 *
 * All measurements are pixels in the outer box's own coordinate space,
 * whose size is `(fw + dx) x (fh + dy)`.
 */
class GlassBox(val fw: Float, val fh: Float, val dx: Float, val dy: Float) {
    // Front-face corners (the rectangle offset down by the top-face depth).
    val fTL = Offset(0f, dy)
    val fTR = Offset(fw, dy)
    val fBR = Offset(fw, fh + dy)
    val fBL = Offset(0f, fh + dy)

    // Receding corners.
    val bTL = Offset(dx, 0f)          // back-top-left  (mouth)
    val bTR = Offset(fw + dx, 0f)     // back-top-right (mouth)
    val bBR = Offset(fw + dx, fh)     // back-bottom-right (side wall)
}

private fun quad(a: Offset, b: Offset, c: Offset, d: Offset) = Path().apply {
    moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(d.x, d.y); close()
}

fun DrawScope.drawGlassBoxBack(box: GlassBox, base: Color) = with(box) {
    // Right side wall — the darkest glass face.
    drawPath(
        quad(fTR, bTR, bBR, fBR),
        Brush.linearGradient(
            listOf(
                lerp(base, Color(0xFF4A7799), 0.5f).copy(alpha = 0.92f),
                lerp(base, Color(0xFF2F5674), 0.62f).copy(alpha = 0.95f),
            ),
            start = fTR, end = bBR,
        ),
    )

    // Top face (the mouth) — the lightest, most reflective face.
    drawPath(
        quad(fTL, fTR, bTR, bTL),
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.18f)),
            start = fTL, end = bTR,
        ),
    )
    // Inset opening to suggest the glass wall thickness at the rim.
    val ctr = Offset((fTL.x + fTR.x + bTR.x + bTL.x) / 4f, (fTL.y + fTR.y + bTR.y + bTL.y) / 4f)
    fun inset(p: Offset, t: Float) = Offset(p.x + (ctr.x - p.x) * t, p.y + (ctr.y - p.y) * t)
    val t = 0.12f
    drawPath(
        quad(inset(fTL, t), inset(fTR, t), inset(bTR, t), inset(bTL, t)),
        color = lerp(base, Color(0xFF35617F), 0.16f),
    )

    // Front face fill.
    drawPath(
        quad(fTL, fTR, fBR, fBL),
        Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.85f),
            0.30f to base,
            1f to lerp(base, Color(0xFFAAC6DA), 0.22f),
            startY = dy, endY = fh + dy,
        ),
    )

    // Far edges — faint (mouth back edge + right-wall verticals).
    val far = Color.White.copy(alpha = 0.28f)
    val farW = 1.5.dp.toPx()
    drawLine(far, bTL, bTR, farW)
    drawLine(far, bTR, bBR, farW)
    drawLine(far, bBR, fBR, farW)
}

fun DrawScope.drawGlassBoxFront(box: GlassBox, edge: Color) = with(box) {
    // Shaded floor for depth.
    drawPath(
        quad(fTL, fTR, fBR, fBL),
        Brush.verticalGradient(
            0.72f to Color.Transparent,
            1f to Color(0x16103A4C),
            startY = dy, endY = fh + dy,
        ),
    )

    // Long vertical reflections down the front glass.
    val top = dy + fh * 0.045f
    val bottom = dy + fh * 0.92f
    drawLine(Color.White.copy(alpha = 0.22f), Offset(fw * 0.11f, top), Offset(fw * 0.11f, bottom), fw * 0.05f)
    drawLine(Color.White.copy(alpha = 0.13f), Offset(fw * 0.20f, top), Offset(fw * 0.20f, bottom), fw * 0.02f)

    // Near edges — bright, uneven glass.
    val edgeBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.95f),
            lerp(edge, Color.White, 0.3f).copy(alpha = 0.4f),
            Color.White.copy(alpha = 0.6f),
        ),
        start = fTL, end = fBR,
    )
    drawPath(quad(fTL, fTR, fBR, fBL), edgeBrush, style = Stroke(width = 2.5.dp.toPx()))

    // The three edges of the near vertical corner (front-right) read as the cube edge.
    drawLine(Color.White.copy(alpha = 0.85f), fTR, fBR, 3.dp.toPx())
    drawLine(Color.White.copy(alpha = 0.9f), fTL, fTR, 3.dp.toPx())          // front rim
    drawLine(Color.White.copy(alpha = 0.55f), fTR, bTR, 2.dp.toPx())        // rising edge to the mouth
    drawLine(Color.White.copy(alpha = 0.5f), fTL, bTL, 2.dp.toPx())         // rising edge, left

    // Corner glow, top-left.
    val glow = Offset(fw * 0.14f, dy + fh * 0.06f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
            center = glow, radius = fw * 0.4f,
        ),
        radius = fw * 0.4f,
        center = glow,
    )
}
