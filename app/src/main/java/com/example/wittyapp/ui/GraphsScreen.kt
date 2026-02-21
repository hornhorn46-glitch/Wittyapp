package com.example.wittyapp.ui

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

// 🔹 ОБЪЯВЛЕНИЕ GraphSeries (раньше отсутствовало)
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
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            series.forEach { graph: GraphSeries ->
                GraphCard(graph)
            }
        }
    }
}

@Composable
private fun GraphCard(series: GraphSeries) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = series.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LineChart(points = series.points)
        }
    }
}

@Composable
private fun LineChart(points: List<GraphPoint>) {

    if (points.size < 2) {
        Text("Недостаточно данных")
        return
    }

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }

    val rawMinY = points.minOf { it.y }
    val rawMaxY = points.maxOf { it.y }

    val paddingY = (rawMaxY - rawMinY) * 0.15f
    val minY = rawMinY - paddingY
    val maxY = rawMaxY + paddingY

    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {

        val width = size.width
        val height = size.height

        fun scaleX(x: Float): Float {
            val dx = (maxX - minX).coerceAtLeast(0.0001f)
            return (x - minX) / dx * width
        }

        fun scaleY(y: Float): Float {
            val dy = (maxY - minY).coerceAtLeast(0.0001f)
            return height - (y - minY) / dy * height
        }

        val path = Path()

        points.forEachIndexed { index, point ->
            val x = scaleX(point.x)
            val y = scaleY(point.y)

            if (index == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f)
        )
    }
}