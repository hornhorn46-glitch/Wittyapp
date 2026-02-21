package com.example.wittyapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wittyapp.ui.SpaceWeatherViewModel

@Composable
fun NowScreen(
    vm: SpaceWeatherViewModel,
    onOpenGraphs: () -> Unit
) {

    val state = vm.state

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Box(Modifier.fillMaxSize()) {

        AsyncImage(
            model = "https://images.unsplash.com/photo-1470259078422-826894b933aa?q=80&w=2000",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
        )

        SnowLayer()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text("Сейчас", style = MaterialTheme.typography.headlineMedium)

                IconButton(onClick = onOpenGraphs) {
                    Icon(Icons.Default.ShowChart, contentDescription = "Графики")
                }
            }

            AuroraCard(state)

            MetricRow("Kp", state.kpNow, state.kp3hAvg)
            MetricRow("Bz", state.bzNow, state.bz3hAvg)
            MetricRow("Speed", state.speedNow, state.speed3hAvg)
            MetricRow("Density", state.densityNow, state.density3hAvg)
        }

        if (state.loading) {
            BottomLoadingSheet()
        }
    }
}

@Composable
private fun AuroraCard(state: com.example.wittyapp.ui.SpaceWeatherUiState) {

    val color = when {
        state.auroraScore > 80 -> Color(0xFF00FFB3)
        state.auroraScore > 60 -> Color(0xFF00C3FF)
        else -> Color(0xFFFFC107)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Прогноз сияний", style = MaterialTheme.typography.titleLarge)
            Text(
                if (state.kpNow == null)
                    "Загрузка данных..."
                else
                    "${state.auroraScore}/100 — ${state.auroraTitle}"
            )
            LinearProgressIndicator(
                progress = { state.auroraScore / 100f },
                color = color
            )
        }
    }
}

@Composable
private fun MetricRow(title: String, now: Double?, avg: Double?) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title)
            Text("Now: ${now ?: "—"} | 3h: ${avg ?: "—"}")
        }
    }
}

@Composable
private fun SnowLayer() {

    val infinite = rememberInfiniteTransition(label = "snow")

    val offset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    Canvas(Modifier.fillMaxSize()) {
        for (i in 0..120) {
            val x = (i * 37f) % size.width
            val y = (i * 53f + offset) % size.height
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = 2f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun BottomLoadingSheet() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.85f)
            )
        ) {
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
                Text("Загрузка данных...")
            }
        }
    }
}