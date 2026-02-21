package com.example.wittyapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wittyapp.ui.SpaceWeatherViewModel

@Composable
fun AuroraScreen(vm: SpaceWeatherViewModel) {
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Сияния", style = MaterialTheme.typography.headlineSmall)
                Text("по метрикам и картам", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { vm.refresh() }, enabled = !s.loading) {
                Text("Обновить")
            }
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Оценка сейчас", style = MaterialTheme.typography.titleMedium)
                Text("${s.auroraScore}/100 — ${s.auroraTitle}", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Считаем по последним 3 часам: Kp + Bz + скорость + плотность.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Карта прогноза (быстро)", style = MaterialTheme.typography.titleMedium)
                AsyncImage(
                    model = "https://services.swpc.noaa.gov/images/aurora-forecast-northern-hemisphere.jpg",
                    contentDescription = "Aurora forecast map",
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                )
                Text(
                    "Если картинка не загрузилась — SWPC поменяли имя файла, обновим ссылку в v2.1.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}