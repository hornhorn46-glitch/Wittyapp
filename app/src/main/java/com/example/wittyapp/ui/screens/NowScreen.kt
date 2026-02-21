package com.example.wittyapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wittyapp.R
import com.example.wittyapp.ui.SpaceWeatherUiState
import com.example.wittyapp.ui.SpaceWeatherViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun NowScreen(
    vm: SpaceWeatherViewModel,
    onOpenGraphs: () -> Unit
) {
    val state = vm.state

    LaunchedEffect(Unit) {
        vm.refresh()
        vm.startAutoRefresh(10 * 60 * 1000L)
    }

    Box(Modifier.fillMaxSize()) {

        // 🌌 local aurora photo background (no network)
        Image(
            painter = painterResource(id = R.drawable.aurora_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.95f
        )

        // dark overlay for readability
        Canvas(Modifier.fillMaxSize()) { drawRect(Color(0xAA000000)) }

        // ❄ snow
        SnowLayer()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TopRow(
                loading = state.loading,
                updatedAt = state.updatedAt?.let(::formatUpdatedAt) ?: "—",
                onRefresh = { vm.refresh() },
                onOpenGraphs = onOpenGraphs
            )

            state.error?.let { ErrorCard(it) }

            AuroraCard(state)

            MetricRow("Kp", state.kpNow, state.kp3hAvg)
            MetricRow("Bz (нТ)", state.bzNow, state.bz3hAvg)
            MetricRow("Speed (км/с)", state.speedNow, state.speed3hAvg)
            MetricRow("Density (1/см³)", state.densityNow, state.density3hAvg)

            Spacer(Modifier.height(90.dp))
        }

        LoadingToastSheet(visible = state.loading)
    }
}

/* ---------------- UI pieces ---------------- */

@Composable
private fun TopRow(
    loading: Boolean,
    updatedAt: String,
    onRefresh: () -> Unit,
    onOpenGraphs: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text("Сейчас", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                "обновлено: $updatedAt",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.80f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = onOpenGraphs) {
                Icon(Icons.Default.ShowChart, contentDescription = "Графики", tint = Color.White)
            }
            IconButton(onClick = onRefresh, enabled = !loading) {
                Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ErrorCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(text, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun AuroraCard(state: SpaceWeatherUiState) {
    val progress by animateFloatAsState(
        targetValue = (state.auroraScore.coerceIn(0, 100) / 100f),
        label = "auroraProgress"
    )

    val accent = when {
        state.auroraScore >= 85 -> Color(0xFF00FFB3)
        state.auroraScore >= 70 -> Color(0xFF00C3FF)
        state.auroraScore >= 45 -> Color(0xFF7C4DFF)
        else -> Color(0xFFFFC107)
    }

    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Прогноз сияний (3 часа)", style = MaterialTheme.typography.titleLarge, color = Color.White)

            Text(
                if (state.kpNow == null) "Загрузка данных…" else "${state.auroraScore}/100 — ${state.auroraTitle}",
                color = Color.White.copy(alpha = 0.90f)
            )

            LinearProgressIndicator(
                progress = { progress },
                color = accent,
                trackColor = Color.White.copy(alpha = 0.20f)
            )

            if (state.auroraDetails.isNotBlank()) {
                Text(
                    state.auroraDetails,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun MetricRow(title: String, now: Double?, avg: Double?) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White)
            Text(
                "Now: ${now?.let { formatNum(it) } ?: "—"} | 3ч: ${avg?.let { formatNum(it) } ?: "—"}",
                color = Color.White.copy(alpha = 0.90f)
            )
        }
    }
}

@Composable
private fun LoadingToastSheet(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.80f))
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Text("Данные обновляются…", color = Color.White)
                }
            }
        }
    }
}

/* ---------------- Snow ---------------- */

@Composable
private fun SnowLayer() {
    val particles = remember {
        List(140) {
            SnowParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                r = 1.0f + Random.nextFloat() * 2.6f,
                speed = 0.08f + Random.nextFloat() * 0.35f
            )
        }
    }

    val t by rememberInfiniteTransition(label = "snow")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(16000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "snowT"
        )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val px = p.x * w
            val py = ((p.y + t * p.speed) % 1f) * h
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = p.r,
                center = Offset(px, py)
            )
        }
    }
}

private data class SnowParticle(val x: Float, val y: Float, val r: Float, val speed: Float)

/* ---------------- Format helpers ---------------- */

private fun formatNum(v: Double): String =
    if (abs(v) >= 100) String.format("%.0f", v) else String.format("%.1f", v)

private fun formatUpdatedAt(i: Instant): String {
    val z = ZoneId.systemDefault()
    val dt = i.atZone(z).toLocalDateTime()
    val f = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    return dt.format(f)
}