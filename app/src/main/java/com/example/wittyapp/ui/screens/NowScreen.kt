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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wittyapp.R
import com.example.wittyapp.ui.SpaceWeatherUiState
import com.example.wittyapp.ui.SpaceWeatherViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.*
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

    var helpFor by remember { mutableStateOf<HelpTopic?>(null) }

    Box(Modifier.fillMaxSize()) {

        // 🌌 local aurora photo
        Image(
            painter = painterResource(id = R.drawable.aurora_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.96f
        )

        // readable overlay
        Canvas(Modifier.fillMaxSize()) { drawRect(Color(0xAA000000)) }

        SnowLayerWithWind()

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

            // KPI gauges + help
            GaugeCard(
                title = "Kp индекс",
                value = state.kpNow,
                unit = "",
                min = 0.0,
                max = 9.0,
                goodRange = 0.0..3.0,
                warnRange = 3.0..6.0,
                badRange = 6.0..9.0,
                onHelp = { helpFor = HelpTopic.KP }
            )

            GaugeCard(
                title = "Скорость ветра",
                value = state.speedNow,
                unit = "км/с",
                min = 250.0,
                max = 950.0,
                goodRange = 250.0..450.0,
                warnRange = 450.0..600.0,
                badRange = 600.0..950.0,
                onHelp = { helpFor = HelpTopic.SPEED }
            )

            GaugeCard(
                title = "Плотность",
                value = state.densityNow,
                unit = "1/см³",
                min = 0.0,
                max = 60.0,
                goodRange = 0.0..12.0,
                warnRange = 12.0..25.0,
                badRange = 25.0..60.0,
                onHelp = { helpFor = HelpTopic.DENSITY }
            )

            // Bz “компас” (стрелка вверх/вниз) + подсказка
            BzCompassCard(
                bz = state.bzNow,
                onHelp = { helpFor = HelpTopic.BZ }
            )

            // Мини-карточки Now/3h внизу, чтобы числа тоже были
            MetricsMini(state, onHelp = { helpFor = it })

            // 🐸 фишка
            FrogEasterEgg()

            Spacer(Modifier.height(90.dp))
        }

        LoadingToastSheet(visible = state.loading)

        helpFor?.let { topic ->
            HelpDialog(topic = topic, onClose = { helpFor = null })
        }
    }
}

/* ---------------- top / loading / error ---------------- */

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

/* ---------------- aurora card ---------------- */

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

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Прогноз сияний (3 часа)", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }

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

/* ---------------- gauges + help ---------------- */

@Composable
private fun GaugeCard(
    title: String,
    value: Double?,
    unit: String,
    min: Double,
    max: Double,
    goodRange: ClosedFloatingPointRange<Double>,
    warnRange: ClosedFloatingPointRange<Double>,
    badRange: ClosedFloatingPointRange<Double>,
    onHelp: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onHelp) {
                    Icon(Icons.Outlined.Info, contentDescription = "info", tint = Color.White.copy(alpha = 0.9f))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Gauge(
                    value = value,
                    min = min,
                    max = max,
                    goodRange = goodRange,
                    warnRange = warnRange,
                    badRange = badRange,
                    modifier = Modifier.size(120.dp)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = value?.let { "${formatNum(it)} $unit" } ?: "—",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Диапазон: ${formatNum(min)}..${formatNum(max)}",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun Gauge(
    value: Double?,
    min: Double,
    max: Double,
    goodRange: ClosedFloatingPointRange<Double>,
    warnRange: ClosedFloatingPointRange<Double>,
    badRange: ClosedFloatingPointRange<Double>,
    modifier: Modifier = Modifier
) {
    val v = value?.coerceIn(min, max)
    val t = if (v == null) null else ((v - min) / (max - min)).toFloat().coerceIn(0f, 1f)

    val col = when {
        v == null -> Color.White.copy(alpha = 0.6f)
        v in badRange -> Color(0xFFFF5252)
        v in warnRange -> Color(0xFFFFC107)
        else -> Color(0xFF00FFB3)
    }

    Canvas(modifier) {
        val stroke = 12f
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        val rect = Rect(c.x - r + stroke, c.y - r + stroke, c.x + r - stroke, c.y + r - stroke)

        // base arc
        drawArc(
            color = Color.White.copy(alpha = 0.18f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // value arc
        if (t != null) {
            drawArc(
                color = col,
                startAngle = 180f,
                sweepAngle = 180f * t,
                useCenter = false,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // needle
            val ang = Math.toRadians((180.0 + 180.0 * t).coerceIn(180.0, 360.0))
            val nx = c.x + cos(ang).toFloat() * (r - stroke * 1.3f)
            val ny = c.y + sin(ang).toFloat() * (r - stroke * 1.3f)
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = c,
                end = Offset(nx, ny),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawCircle(Color.White, radius = 6f, center = c)
        } else {
            drawCircle(Color.White.copy(alpha = 0.25f), radius = 6f, center = c)
        }
    }
}

@Composable
private fun BzCompassCard(bz: Double?, onHelp: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Bz направление", color = Color.White, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onHelp) {
                    Icon(Icons.Outlined.Info, contentDescription = "info", tint = Color.White.copy(alpha = 0.9f))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                BzCompass(bz = bz, modifier = Modifier.size(120.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = bz?.let { "${formatNum(it)} нТ" } ?: "—",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Ниже 0 — лучше для сияний",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun BzCompass(bz: Double?, modifier: Modifier = Modifier) {
    val angle = when {
        bz == null -> 270f // вниз по умолчанию
        bz < 0 -> 270f // вниз (South)
        else -> 90f // вверх (North)
    }
    val strength = bz?.let { abs(it).coerceAtMost(20.0) / 20.0 }?.toFloat() ?: 0.4f
    val arrowColor = when {
        bz == null -> Color.White.copy(alpha = 0.6f)
        bz <= -6 -> Color(0xFFFF5252)
        bz < 0 -> Color(0xFFFFC107)
        else -> Color(0xFF00FFB3)
    }

    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f

        // ring sectors (down=best)
        drawArc(Color(0xFF00FFB3).copy(alpha = 0.18f), 210f, 120f, false, style = Stroke(12f, cap = StrokeCap.Round))
        drawArc(Color(0xFFFFC107).copy(alpha = 0.18f), 330f, 60f, false, style = Stroke(12f, cap = StrokeCap.Round))
        drawArc(Color(0xFFFF5252).copy(alpha = 0.18f), 30f, 180f, false, style = Stroke(12f, cap = StrokeCap.Round))

        // outline
        drawCircle(Color.White.copy(alpha = 0.20f), radius = r - 8f, center = c, style = Stroke(2f))

        // arrow
        val ang = Math.toRadians(angle.toDouble())
        val len = (r - 18f) * (0.55f + 0.45f * strength)
        val tip = Offset(c.x + cos(ang).toFloat() * len, c.y + sin(ang).toFloat() * len)

        drawLine(
            color = arrowColor,
            start = c,
            end = tip,
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        drawCircle(arrowColor, radius = 6f, center = c)
        drawCircle(arrowColor, radius = 5f, center = tip)
    }
}

/* ---------------- mini metrics (numbers + help icons) ---------------- */

@Composable
private fun MetricsMini(state: SpaceWeatherUiState, onHelp: (HelpTopic) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Показатели (Now / 3ч)", color = Color.White, style = MaterialTheme.typography.titleMedium)

            MiniRow("Kp", state.kpNow, state.kp3hAvg, onHelp = { onHelp(HelpTopic.KP) })
            MiniRow("Bz (нТ)", state.bzNow, state.bz3hAvg, onHelp = { onHelp(HelpTopic.BZ) })
            MiniRow("Speed (км/с)", state.speedNow, state.speed3hAvg, onHelp = { onHelp(HelpTopic.SPEED) })
            MiniRow("Density", state.densityNow, state.density3hAvg, onHelp = { onHelp(HelpTopic.DENSITY) })
        }
    }
}

@Composable
private fun MiniRow(label: String, now: Double?, avg: Double?, onHelp: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White)
            IconButton(onClick = onHelp, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.85f))
            }
        }
        Text(
            "Now: ${now?.let { formatNum(it) } ?: "—"} | 3ч: ${avg?.let { formatNum(it) } ?: "—"}",
            color = Color.White.copy(alpha = 0.90f),
            textAlign = TextAlign.End
        )
    }
}

/* ---------------- help dialog ---------------- */

private enum class HelpTopic { KP, BZ, SPEED, DENSITY }

@Composable
private fun HelpDialog(topic: HelpTopic, onClose: () -> Unit) {
    val (title, text) = when (topic) {
        HelpTopic.KP -> "Что такое Kp?" to
            "Kp — индекс геомагнитной активности (0..9).\n" +
            "Чем выше Kp, тем сильнее возмущение магнитосферы.\n" +
            "Для сияний обычно нужен рост Kp.\n" +
            "Но для реального «шанса увидеть» важен ещё и Bz."
        HelpTopic.BZ -> "Что такое Bz?" to
            "Bz — компонент магнитного поля солнечного ветра.\n" +
            "Когда Bz отрицательный (ниже 0), энергия легче «закачивается» в магнитосферу.\n" +
            "Сильно отрицательный Bz (например ниже -6 нТ) — хороший знак для сияний."
        HelpTopic.SPEED -> "Скорость солнечного ветра" to
            "Скорость (км/с) показывает, насколько быстро поток солнечного ветра.\n" +
            "Высокая скорость часто усиливает геомагнитную активность.\n" +
            "Важнее всего сочетание: скорость + отрицательный Bz."
        HelpTopic.DENSITY -> "Плотность" to
            "Плотность показывает, сколько частиц в солнечном ветре.\n" +
            "Рост плотности может усилить воздействие на магнитосферу.\n" +
            "Но сама по себе плотность не гарантирует сияние — смотрим вместе с Bz и скоростью."
    }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text("Ок") } },
        title = { Text(title) },
        text = { Text(text) }
    )
}

/* ---------------- snow with wind (random + periodic wind) ---------------- */

@Composable
private fun SnowLayerWithWind() {
    val particles = remember {
        List(140) {
            SnowParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                r = 1.0f + Random.nextFloat() * 2.8f,
                speedY = 0.08f + Random.nextFloat() * 0.45f,
                drift = (Random.nextFloat() - 0.5f) * 0.35f
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

    // периодический "ветер" туда-сюда
    val wind by rememberInfiniteTransition(label = "wind")
        .animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(6200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "windX"
        )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val pxBase = p.x * w
            val py = ((p.y + t * p.speedY) % 1f) * h

            // лёгкая случайность + ветер
            val windX = wind * 18f
            val px = (pxBase + windX + sin((py / h) * 6.28f) * 6f * p.drift).mod(w)

            drawCircle(
                color = Color.White.copy(alpha = 0.33f),
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
    val speedY: Float,
    val drift: Float
)

/* ---------------- frog easter egg ---------------- */

@Composable
private fun FrogEasterEgg() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Фишка дня", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text("Пиксельная лягушка приносит удачу сияниям.", color = Color.White.copy(alpha = 0.85f))
            }
            PixelFrog(modifier = Modifier.size(72.dp))
        }
    }
}

@Composable
private fun PixelFrog(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val px = size.minDimension / 12f
        fun rect(x: Int, y: Int, c: Color) {
            drawRect(
                color = c,
                topLeft = Offset(x * px, y * px),
                size = androidx.compose.ui.geometry.Size(px, px)
            )
        }

        val green = Color(0xFF7CFF6B)
        val dark = Color(0xFF2A6B2A)
        val white = Color.White
        val black = Color.Black
        val pink = Color(0xFFFF7AA2)

        // body
        for (y in 4..9) for (x in 3..8) rect(x, y, green)
        // head top
        for (x in 4..7) rect(x, 3, green)
        // eyes
        rect(3, 3, green); rect(8, 3, green)
        rect(3, 2, green); rect(8, 2, green)
        rect(3, 1, green); rect(8, 1, green)
        rect(3, 0, dark);  rect(8, 0, dark)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)
        rect(3, 2, white); rect(8, 2, white)

        rect(3, 2, white); rect(8, 2, white)

        // pupils
        rect(3, 2, black); rect(8, 2, black)

        // mouth
        rect(4, 8, dark); rect(5, 9, pink); rect(6, 9, pink); rect(7, 8, dark)
    }
}

/* ---------------- helpers ---------------- */

private fun formatNum(v: Double): String =
    if (abs(v) >= 100) String.format("%.0f", v) else String.format("%.1f", v)

private fun formatUpdatedAt(i: Instant): String {
    val z = ZoneId.systemDefault()
    val dt = i.atZone(z).toLocalDateTime()
    val f = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    return dt.format(f)
}