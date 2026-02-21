package com.example.wittyapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AuroraScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Сияния", style = MaterialTheme.typography.headlineSmall)

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NOAA Aurora 30-min Forecast", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Ниже — картинка/карта прогноза (30–90 минут).",
                    style = MaterialTheme.typography.bodySmall
                )

                // Простая “картинка прогноза”. Если URL поменяется — заменим на актуальный из SWPC.
                AsyncImage(
                    model = "https://services.swpc.noaa.gov/images/aurora-forecast-northern-hemisphere.jpg",
                    contentDescription = "Aurora forecast",
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                )

                Text(
                    "Если изображение не грузится — это значит, что SWPC поменяли имя файла. Тогда обновим URL.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DONKI / ENLIL", style = MaterialTheme.typography.titleMedium)
                Text(
                    "В v1 показываем события на вкладке «События». " +
                        "В следующей версии добавим картинки ENLIL, связанные с CME-анализом.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}