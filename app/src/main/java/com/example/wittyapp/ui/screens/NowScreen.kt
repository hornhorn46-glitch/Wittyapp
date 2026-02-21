package com.example.wittyapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.wittyapp.ui.SpaceWeatherViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NowScreen(vm: SpaceWeatherViewModel) {
    val s = vm.state

    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Header(
            title = "Сейчас",
            subtitle = s.updatedAt?.let { fmtInstant(it) },
            loading = s.loading,
            onRefresh = { vm.refresh() }
        )

        s.error?.let { ErrorCard(it) }

        AuroraCard(score = s.auroraScore, title = s.auroraTitle, details = s.auroraDetails)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Kp",
                value = s.kpNow?.let { "%.1f".format(it) } ?: "—",
                hint = s.kp3hAvg?.let { "3ч: %.1f".format(it) } ?: "3ч: —",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Bz (нТ)",
                value = s.bzNow?.let { "%.1f".format(it) } ?: "—",
                hint = s.bz3hAvg?.let { "3ч: %.1f".format(it) } ?: "3ч: —",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Скорость (км/с)",
                value = s.speedNow?.let { "%.0f".format(it) } ?: "—",
                hint = s.speed3hAvg?.let { "3ч: %.0f".format(it) } ?: "3ч: —",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Плотность (1/см³)",
                value = s.densityNow?.let { "%.1f".format(it) } ?: "—",
                hint = s.density3hAvg?.let { "3ч: %.1f".format(it) } ?: "3ч: —",
                modifier = Modifier.weight(1f)
            )
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Как читать показатели", style = MaterialTheme.typography.titleMedium)
                Text("• Kp — общий уровень возмущений. Чем выше, тем лучше шанс сияний.", style = MaterialTheme.typography.bodyMedium)
                Text("• Bz < 0 (особенно долго) — хороший знак для сияний.", style = MaterialTheme.typography.bodyMedium)
                Text("• Высокая скорость и плотность усиливают эффект.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String?, loading: Boolean, onRefresh: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle ?: "—", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onRefresh, enabled = !loading) {
            Text(if (loading) "..." else "Обновить")
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
private fun MetricCard(title: String, value: String, hint: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.displaySmall)
            Text(hint, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AuroraCard(score: Int, title: String, details: String) {
    val anim by animateFloatAsState(targetValue = score / 100f, label = "score")
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Прогноз сияний (3 часа)", style = MaterialTheme.typography.titleLarge)
            Text(title, style = MaterialTheme.typography.titleMedium)

            LinearProgressIndicator(progress = { anim })

            Text(details, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun fmtInstant(i: java.time.Instant): String {
    val z = ZoneId.systemDefault()
    val dt = i.atZone(z).toLocalDateTime()
    val f = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    return "обновлено: ${dt.format(f)}"
}