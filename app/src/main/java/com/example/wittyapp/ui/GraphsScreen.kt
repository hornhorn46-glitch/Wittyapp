package com.example.wittyapp.ui

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            series.forEach { s ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.06f)
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(s.name)
                        LineChart(s.points)
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChart(points: List<GraphPoint>) {

    if (points.size < 2) return

    val rawMin = points.minOf { it.y }
    val rawMax = points.maxOf { it.y }

    val padY = (rawMax - rawMin) * 0.15f
    val minY = rawMin - padY
    val maxY = rawMax + padY

    Canvas(Modifier.fillMaxWidth().height(220.dp)) {

        val w = size.width
        val h = size.height

        fun sx(x: Float) = x / points.maxOf { it.x } * w
        fun sy(y: Float) =
            h - ((y - minY) / (maxY - minY).coerceAtLeast(0.001f) * h)

        val path = Path()
        points.forEachIndexed { i, p ->
            val x = sx(p.x)
            val y = sy(p.y)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = MaterialTheme.colorScheme.primary,
            style = Stroke(width = 5f)
        )
    }
}