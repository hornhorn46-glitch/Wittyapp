package com.example.wittyapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.wittyapp.domain.GraphPoint

data class GraphSeries(
    val name: String,
    val hint: String,
    val points: List<GraphPoint>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphsScreen(
    title: String,
    series: List<GraphSeries>,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = { TextButton(onClick = onClose) { Text("Закрыть") } }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            series.forEach { s ->
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s.name, style = MaterialTheme.typography.titleMedium)
                            Text(s.hint, style = MaterialTheme.typography.bodySmall)
                        }

                        val lineColor = MaterialTheme.colorScheme.primary
                        val gridColor = MaterialTheme.colorScheme.outlineVariant
                        val pointColor = MaterialTheme.colorScheme.tertiary

                        LineChart(
                            points = s.points,
                            lineColor = lineColor,
                            gridColor = gridColor,
                            pointColor = pointColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChart(
    points: List<GraphPoint>,
    lineColor: Color,
    gridColor: Color,
    pointColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Text("Недостаточно данных")
        return
    }

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }

    val rawMinY = points.minOf { it.y }
    val rawMaxY = points.maxOf { it.y }
    val padY = (rawMaxY - rawMinY).let { if (it == 0f) 1f else it } * 0.15f
    val minY = rawMinY - padY
    val maxY = rawMaxY + padY

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pad = 12f

        fun sx(x: Float): Float {
            val dx = (maxX - minX).coerceAtLeast(0.0001f)
            return pad + (x - minX) / dx * (w - 2 * pad)
        }

        fun sy(y: Float): Float {
            val dy = (maxY - minY).coerceAtLeast(0.0001f)
            return (h - pad) - (y - minY) / dy * (h - 2 * pad)
        }

        // grid
        val gridN = 4
        for (i in 0..gridN) {
            val yy = pad + i * (h - 2 * pad) / gridN
            drawLine(
                color = gridColor,
                start = Offset(pad, yy),
                end = Offset(w - pad, yy),
                strokeWidth = 1f
            )
        }

        val path = Path()
        points.forEachIndexed { idx, p ->
            val x = sx(p.x)
            val y = sy(p.y)
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path = path, color = lineColor, style = Stroke(width = 4f))

        // last point
        points.lastOrNull()?.let { last ->
            drawCircle(
                color = pointColor,
                radius = 7f,
                center = Offset(sx(last.x), sy(last.y))
            )
        }
    }
}