package com.example.wittyapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.wittyapp.domain.GraphPoint

data class GraphSeries(
    val name: String,
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
                actions = {
                    TextButton(onClick = onClose) {
                        Text("Закрыть")
                    }
                }
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            series.forEach { graph ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(graph.name)
                        LineChart(graph.points)
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChart(points: List<GraphPoint>) {

    if (points.size < 2) return

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }

    val rawMinY = points.minOf { it.y }
    val rawMaxY = points.maxOf { it.y }

    val paddingY = (rawMaxY - rawMinY) * 0.15f
    val minY = rawMinY - paddingY
    val maxY = rawMaxY + paddingY

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {

        val w = size.width
        val h = size.height

        fun sx(x: Float) = (x - minX) / (maxX - minX).coerceAtLeast(0.0001f) * w
        fun sy(y: Float) = h - (y - minY) / (maxY - minY).coerceAtLeast(0.0001f) * h

        val path = Path()

        points.forEachIndexed { i, p ->
            val x = sx(p.x)
            val y = sy(p.y)
            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = MaterialTheme.colorScheme.primary,
            style = Stroke(width = 4f)
        )
    }
}