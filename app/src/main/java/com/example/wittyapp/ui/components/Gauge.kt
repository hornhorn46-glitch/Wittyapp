package com.example.wittyapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    title: String,
    value: Float,
    unit: String,
    minValue: Float,
    maxValue: Float,
    warnThreshold: Float? = null,
    dangerThreshold: Float? = null,
    modifier: Modifier = Modifier
) {
    val t = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    val anim = animateFloatAsState(t, label = "gauge").value

    val base = Color.White.copy(alpha = 0.16f)
    val track = Color.White.copy(alpha = 0.10f)
    val accent = MaterialTheme.colorScheme.primary

    // цвет стрелки/дуги по порогам (без "тяжёлых" текстур)
    val vColor = when {
        dangerThreshold != null && value >= dangerThreshold -> Color(0xFFFF5252)
        warnThreshold != null && value >= warnThreshold -> Color(0xFFFFB300)
        else -> accent
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(150.dp)) {
                val w = size.width
                val h = size.height
                val r = min(w, h) * 0.42f
                val c = Offset(w / 2f, h / 2f)
                val start = 160f
                val sweep = 220f
                val strokeW = 14f

                // "корпус" + лёгкая тень
                drawCircle(Color.Black.copy(alpha = 0.22f), radius = r * 1.18f, center = c + Offset(2f, 2f))
                drawCircle(base, radius = r * 1.18f, center = c)

                // трек
                drawArc(
                    color = track,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(strokeW, cap = StrokeCap.Round),
                    topLeft = Offset(c.x - r, c.y - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
                )

                // заполнение — градиентом
                val grad = Brush.linearGradient(
                    colors = listOf(vColor.copy(alpha = 0.65f), vColor),
                    start = Offset(c.x - r, c.y - r),
                    end = Offset(c.x + r, c.y + r)
                )
                drawArc(
                    brush = grad,
                    startAngle = start,
                    sweepAngle = sweep * anim,
                    useCenter = false,
                    style = Stroke(strokeW, cap = StrokeCap.Round),
                    topLeft = Offset(c.x - r, c.y - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
                )

                // стрелка
                val a = (start + sweep * anim) * (PI / 180f)
                val p0 = Offset(c.x + cos(a).toFloat() * (r * 0.05f), c.y + sin(a).toFloat() * (r * 0.05f))
                val p1 = Offset(c.x + cos(a).toFloat() * (r * 0.95f), c.y + sin(a).toFloat() * (r * 0.95f))
                drawLine(Color.Black.copy(alpha = 0.25f), p0 + Offset(1f, 1f), p1 + Offset(1f, 1f), strokeWidth = 6f, cap = StrokeCap.Round)
                drawLine(vColor, p0, p1, strokeWidth = 6f, cap = StrokeCap.Round)

                // центр
                drawCircle(Color.Black.copy(alpha = 0.25f), radius = r * 0.12f, center = c + Offset(1f, 1f))
                drawCircle(Color.White.copy(alpha = 0.85f), radius = r * 0.12f, center = c)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.9f))
                Text(
                    text = "${value.toInt()} $unit",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
