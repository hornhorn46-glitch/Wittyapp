package com.example.wittyapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import kotlin.random.Random

@Composable
fun NowScreen(vm: SpaceWeatherViewModel) {

    val state = vm.state
    val scroll = rememberScrollState()

    val auroraImage = if (state.auroraScore > 70)
        "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=2000"
    else
        "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?q=80&w=2000"

    val infinite = rememberInfiniteTransition(label = "bg")

    val glowShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(40000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "shift"
    )

    Box(Modifier.fillMaxSize()) {

        // 🌌 Aurora image
        AsyncImage(
            model = auroraImage,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = glowShift * 0.02f
                },
            contentScale = ContentScale.Crop,
            alpha = 0.95f
        )

        // 🌈 Moving gradient glow
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xAA000000),
                            Color(0x66000000),
                            Color(0xAA000000)
                        )
                    )
                )
        )

        // ✨ Stars
        StarLayer()

        // 📦 Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Header(
                    loading = state.loading,
                    onRefresh = { vm.refresh() }
                )
            }

            AuroraCard(state)

            MetricsBlock(state)
        }
    }
}