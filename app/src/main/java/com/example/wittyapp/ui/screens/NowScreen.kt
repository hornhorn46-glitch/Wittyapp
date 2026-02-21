package com.example.wittyapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wittyapp.domain.*
import com.example.wittyapp.net.SpaceWeatherApi
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

@Composable
fun NowScreen(api: SpaceWeatherApi) {
    val scope = rememberCoroutineScope()

    var kpRaw by remember { mutableStateOf<JsonElement?>(null) }
    var plasmaRaw by remember { mutableStateOf<JsonElement?>(null) }
    var magRaw by remember { mutableStateOf<JsonElement?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                kpRaw = api.fetchKp()
                plasmaRaw = api.fetchSolarWindPlasma1d()
                magRaw = api.fetchSolarWindMag1d()
            } catch (t: Throwable) {
                error = t.message ?: "Ошибка загрузки"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    val kp = kpRaw?.let(::parseKpNow)
    val plasma = plasmaRaw?.let(::parsePlasmaNow)
    val bz = magRaw?.let(::parseMagBzNow)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Космическая погода", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { refresh() }, enabled = !loading) { Text("Обновить") }
        }

        if (error != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(error!!, modifier = Modifier.padding(12.dp))
            }
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Kp сейчас", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = kp?.kp?.toString() ?: "—",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = kp?.let { "Время: ${it.timeTag}" } ?: "Время: —",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = kp?.kp?.let { kpLabel(it) } ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Солнечный ветер", style = MaterialTheme.typography.titleMedium)
                val speed = plasma?.second?.first
                val dens = plasma?.second?.second
                Text("Скорость: ${speed?.let { "%.0f".format(it) } ?: "—"} км/с")
                Text("Плотность: ${dens?.let { "%.1f".format(it) } ?: "—"} 1/см³")
                Text("Bz: ${bz?.second?.let { "%.1f".format(it) } ?: "—"} нТ")
                Text(
                    text = "Отрицательный Bz повышает шанс возмущений (если держится).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun kpLabel(kp: Double): String = when {
    kp < 3 -> "Тихо"
    kp < 5 -> "Возбуждённо"
    kp < 6 -> "Слабая буря"
    kp < 7 -> "Умеренная буря"
    kp < 8 -> "Сильная буря"
    else -> "Экстремально"
}