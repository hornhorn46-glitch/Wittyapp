package com.example.wittyapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.wittyapp.ui.SpaceWeatherViewModel

@Composable
fun EventsScreen(vm: SpaceWeatherViewModel) {
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
                Text("События", style = MaterialTheme.typography.headlineSmall)
                Text("DONKI: CME / FLR / GST", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { vm.refresh() }, enabled = !s.loading) {
                Text(if (s.loading) "..." else "Обновить")
            }
        }

        s.error?.let {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
        }

        if (s.events.isEmpty() && !s.loading) {
            Text("Событий нет (или DONKI недоступен).", style = MaterialTheme.typography.bodyMedium)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f, fill = true)) {
            items(s.events) { e ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${e.type}: ${e.title}", style = MaterialTheme.typography.titleMedium)
                        Text(e.timeTag, style = MaterialTheme.typography.bodySmall)
                        e.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
                Text(
                    "тут был Женя",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}