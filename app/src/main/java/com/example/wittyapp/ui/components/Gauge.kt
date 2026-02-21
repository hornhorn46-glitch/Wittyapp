package com.example.wittyapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val v = value.coerceIn(minValue, maxValue)
    val t = ((v - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)

    val needleColor = when {
        dangerThreshold != null && v >= dangerThreshold -> Color(0xFFFF5252)
        warnThreshold != null && v >= warnThreshold -> Color(0xFFFFB74D)
        else -> Color(0xFF34E7B3)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val c = Offset(w / 2f, h / 2f)
            val r = min(w, h) * 0.40f

            // дуга спидометра: 210°..-30° (как обычно)
            val startDeg = 210f
            val sweepDeg = 240f

            // фон-дуга
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = startDeg,
                sweepAngle = sweepDeg,
                useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(width = 18f, cap = StrokeCap.Round)
            )

            // активная дуга
            drawArc(
                color = needleColor.copy(alpha = 0.95f),
                startAngle = startDeg,
                sweepAngle = sweepDeg * t,
                useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(width = 18f, cap = StrokeCap.Round)
            )

            // стрелка
            val ang = Math.toRadians((startDeg + sweepDeg * t).toDouble())
            val p = Offset(
                c.x + cos(ang).toFloat() * (r * 0.92f),
                c.y + sin(ang).toFloat() * (r * 0.92f)
            )

            drawLine(
                color = Color.Black.copy(alpha = 0.25f),
                start = c + Offset(2f, 2f),
                end = p + Offset(2f, 2f),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = needleColor,
                start = c,
                end = p,
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )

            // центр
            drawCircle(Color.White.copy(alpha = 0.92f), radius = 10f, center = c)
        }

        // ТЕКСТ — ОПУСКАЕМ ВНИЗ
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(42.dp))

            Text(
                title,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            // значение опускаем ниже центра
            val valueStr = formatValue(value)
            Text(
                text = valueStr,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.offset(y = 10.dp),
                textAlign = TextAlign.Center
            )

            if (unit.isNotBlank()) {
                Text(
                    text = unit,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.offset(y = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatValue(v: Float): String {
    // если почти целое — без дробей
    val iv = v.toInt()
    return if (kotlin.math.abs(v - iv.toFloat()) < 0.05f) iv.toString() else String.format("%.1f", v)
}