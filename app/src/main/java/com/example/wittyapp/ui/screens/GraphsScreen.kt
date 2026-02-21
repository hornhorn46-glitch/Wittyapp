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
import androidx.compose.ui.unit.dp
import com.example.wittyapp.domain.GraphPoint

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
    val sp = series.firstOrNull { it.name.startsWith("Скорость") || it.name.startsWith("Speed") }

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

            SegmentedButtons(tab = tab, onTab = { tab = it })

            current?.let { s ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                .height(260.dp)
                        )
                    }
                }
            } ?: Text("Нет данных для графика")
        }
    }
}

@Composable
private fun SegmentedButtons(tab: GraphTab, onTab: (GraphTab) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = tab == GraphTab.KP, onClick = { onTab(GraphTab.KP) }, label = { Text("Kp") })
        FilterChip(selected = tab == GraphTab.BZ, onClick = { onTab(GraphTab.BZ) }, label = { Text("Bz") })
        FilterChip(selected = tab == GraphTab.SPEED, onClick = { onTab(GraphTab.SPEED) }, label = { Text("Speed") })
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

        val gridN = 4
        for (i in 0..gridN) {
            val yy = pad + i * (h - 2 * pad) / gridN
            drawLine(color = gridColor, start = Offset(pad, yy), end = Offset(w - pad, yy), strokeWidth = 1f)
        }

        val path = Path()
        points.forEachIndexed { idx, p ->
            val x = sx(p.x)
            val y = sy(p.y)
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path = path, color = lineColor, style = Stroke(width = 4f))

        points.lastOrNull()?.let { last ->
            drawCircle(color = pointColor, radius = 7f, center = Offset(sx(last.x), sy(last.y)))
        }
    }
}