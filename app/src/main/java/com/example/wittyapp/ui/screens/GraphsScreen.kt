package com.example.wittyapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wittyapp.domain.GraphPoint
import kotlin.math.roundToInt

data class GraphSeries(
    val name: String,
    val hint: String,
    val points: List<GraphPoint>
)

private enum class GraphTab { KP, BZ, SPEED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphsScreen(
    title: String,
    series: List<GraphSeries>,
    onClose: () -> Unit
) {
    var tab by remember { mutableStateOf(GraphTab.KP) }

    val kp = series.firstOrNull { it.name.startsWith("Kp") }
    val bz = series.firstOrNull { it.name.startsWith("Bz") }
    val sp = series.firstOrNull { it.name.contains("Скорость") || it.name.contains("Speed") }

    val current = when (tab) {
        GraphTab.KP -> kp
        GraphTab.BZ -> bz
        GraphTab.SPEED -> sp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = tab == GraphTab.KP, onClick = { tab = GraphTab.KP }, label = { Text("Kp") })
                FilterChip(selected = tab == GraphTab.BZ, onClick = { tab = GraphTab.BZ }, label = { Text("Bz") })
                FilterChip(selected = tab == GraphTab.SPEED, onClick = { tab = GraphTab.SPEED }, label = { Text("Speed") })
            }

            current?.let { s ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s.name, fontWeight = FontWeight.SemiBold)
                        Text(s.hint, style = MaterialTheme.typography.bodySmall)
                        LineChartWithAxes(
                            points = s.points,
                            mode = tab,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        )
                    }
                }
            } ?: Text("Нет данных для графика")
        }
    }
}

@Composable
private fun LineChartWithAxes(
    points: List<GraphPoint>,
    mode: GraphTab,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Text("Недостаточно данных")
        return
    }

    val rawMinX = points.minOf { it.x }
    val rawMaxX = points.maxOf { it.x }
    val rawMinY = points.minOf { it.y }
    val rawMaxY = points.maxOf { it.y }

    val padY = (rawMaxY - rawMinY).let { if (it == 0f) 1f else it } * 0.15f
    val minY = rawMinY - padY
    val maxY = rawMaxY + padY

    val lineGood = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leftPad = 44f
        val bottomPad = 26f
        val topPad = 10f
        val rightPad = 10f

        fun sx(x: Float): Float {
            val dx = (rawMaxX - rawMinX).coerceAtLeast(0.0001f)
            return leftPad + (x - rawMinX) / dx * (w - leftPad - rightPad)
        }

        fun sy(y: Float): Float {
            val dy = (maxY - minY).coerceAtLeast(0.0001f)
            return (h - bottomPad) - (y - minY) / dy * (h - topPad - bottomPad)
        }

        // grid Y
        val gridN = 4
        for (i in 0..gridN) {
            val yy = topPad + i * (h - topPad - bottomPad) / gridN
            drawLine(
                color = gridColor,
                start = Offset(leftPad, yy),
                end = Offset(w - rightPad, yy),
                strokeWidth = 1f
            )
        }

        // axes
        drawLine(gridColor, Offset(leftPad, topPad), Offset(leftPad, h - bottomPad), 2f)
        drawLine(gridColor, Offset(leftPad, h - bottomPad), Offset(w - rightPad, h - bottomPad), 2f)

        // y labels (min/mid/max)
        val yMin = rawMinY
        val yMax = rawMaxY
        val yMid = (rawMinY + rawMaxY) / 2f

        drawContext.canvas.nativeCanvas.apply {
            val p = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(220, 255, 255, 255)
                textSize = 24f
                isAntiAlias = true
            }
            fun txt(v: Float): String = when (mode) {
                GraphTab.KP -> String.format("%.1f", v)
                GraphTab.BZ -> String.format("%.1f", v)
                GraphTab.SPEED -> String.format("%.0f", v)
            }

            drawText(txt(yMax), 2f, sy(yMax) + 8f, p)
            drawText(txt(yMid), 2f, sy(yMid) + 8f, p)
            drawText(txt(yMin), 2f, sy(yMin) + 8f, p)
        }

        // x ticks (0, 6, 12, 18, 24h) — условно по шкале
        val ticks = 4
        for (i in 0..ticks) {
            val x = leftPad + i * (w - leftPad - rightPad) / ticks
            drawLine(gridColor, Offset(x, h - bottomPad), Offset(x, h - bottomPad + 6f), 2f)
            val label = "${(24 - i * (24 / ticks)).roundToInt()}ч"
            drawContext.canvas.nativeCanvas.apply {
                val p = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(220, 255, 255, 255)
                    textSize = 22f
                    isAntiAlias = true
                }
                drawText(label, x - 16f, h - 2f, p)
            }
        }

        // draw segments with conditional coloring
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]

            val color = when (mode) {
                GraphTab.BZ -> if (minOf(a.y, b.y) <= -6f) Color(0xFFFF5252) else lineGood
                GraphTab.SPEED -> if (maxOf(a.y, b.y) >= 600f) Color(0xFFFF5252) else lineGood
                GraphTab.KP -> if (maxOf(a.y, b.y) >= 6f) Color(0xFFFFC107) else lineGood
            }

            drawLine(
                color = color,
                start = Offset(sx(a.x), sy(a.y)),
                end = Offset(sx(b.x), sy(b.y)),
                strokeWidth = 4f
            )
        }

        // last point marker
        points.lastOrNull()?.let { last ->
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(sx(last.x), sy(last.y))
            )
        }
    }
}