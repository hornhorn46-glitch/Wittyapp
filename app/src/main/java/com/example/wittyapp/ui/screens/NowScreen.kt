package com.example.wittyapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wittyapp.ui.SpaceWeatherViewModel

@Composable
fun NowScreen(vm: SpaceWeatherViewModel) {

    val state = vm.state
    val scroll = rememberScrollState()

    Box(Modifier.fillMaxSize()) {

        AsyncImage(
            model = "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?q=80&w=2000",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Header(state.loading) { vm.refresh() }

            AuroraCard(state)

            MetricRow("Kp", state.kpNow, state.kp3hAvg)
            MetricRow("Bz", state.bzNow, state.bz3hAvg)
            MetricRow("Speed", state.speedNow, state.speed3hAvg)
            MetricRow("Density", state.densityNow, state.density3hAvg)
        }
    }
}

@Composable
private fun Header(loading: Boolean, onRefresh: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Сейчас", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onRefresh, enabled = !loading) {
            Text(if (loading) "..." else "Обновить")
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
        ),
        modifier = Modifier.blur(0.5.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Прогноз сияний", style = MaterialTheme.typography.titleLarge)
            Text("${state.auroraScore}/100 — ${state.auroraTitle}")
            LinearProgressIndicator(
                progress = { state.auroraScore / 100f },
                color = color
            )
        }
    }
}

@Composable
private fun MetricRow(
    title: String,
    now: Double?,
    avg: Double?
) {
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