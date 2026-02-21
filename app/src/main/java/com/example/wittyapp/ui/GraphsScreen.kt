package com.example.wittyapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.wittyapp.domain.GraphPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphsScreen(
    title: String,
    subtitle: String,
    series: List<GraphSeries>,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = { TextButton(onClick = onClose) { Text("Закрыть") } }
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
                                .height(180.dp)
                        )
                    }
                }
            }
        }
    }
}

data class GraphSeries(
    val name: String,
    val hint: String,
    val points: List<GraphPoint>
)

@Composable
private fun LineChart(
    points: List<GraphPoint>,
    lineColor: Color,
    gridColor: Color,
    pointColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Box(modifier) { Text("Недостаточно данных") }
        return
    }

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pad = 16f

        fun sx(x: Float): Float {
            val dx = (maxX - minX).coerceAtLeast(1e-6f)
            return pad + (x - minX) / dx * (w - 2 * pad)
        }

        fun sy(y: Float): Float {
            val dy = (maxY - minY).coerceAtLeast(1e-6f)
            return (h - pad) - (y - minY) / dy * (h - 2 * pad)
        }

        // сетка
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

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f)
        )

        // последняя точка
        points.lastOrNull()?.let { last ->
            drawCircle(
                color = pointColor,
                radius = 7f,
                center = Offset(sx(last.x), sy(last.y))
            )
        }
    }
}