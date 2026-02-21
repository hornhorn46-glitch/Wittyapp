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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

        Image(
            painter = painterResource(id = R.drawable.aurora_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.95f
        )

        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xAA000000))
        }

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
                updatedAt = state.updatedAt?.let(::formatUpdatedAt),
                onRefresh = { vm.refresh() },
                onOpenGraphs = onOpenGraphs
            )

            state.error?.let { ErrorCard(it) }

            AuroraCard(state)

            MetricRow("Kp", state.kpNow, state.kp3hAvg)
            MetricRow("Bz", state.bzNow, state.bz3hAvg)
            MetricRow("Speed", state.speedNow, state.speed3hAvg)
            MetricRow("Density", state.densityNow, state.density3hAvg)

            Spacer(Modifier.height(80.dp))
        }

        LoadingToastSheet(visible = state.loading)
    }
}

@Composable
private fun SnowLayer() {

    val particles = remember {
        List(120) {
            SnowParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                r = 1.2f + Random.nextFloat() * 2.5f,
                speed = 0.1f + Random.nextFloat() * 0.3f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "snow")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowAnim"
    )

    Canvas(Modifier.fillMaxSize()) {

        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val px = p.x * w
            val py = ((p.y + progress * p.speed) % 1f) * h

            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = p.r,
                center = Offset(px, py)
            )
        }
    }
}

private data class SnowParticle(
    val x: Float,
    val y: Float,
    val r: Float,
    val speed: Float
)

@Composable
private fun LoadingToastSheet(visible: Boolean) {

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                )
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