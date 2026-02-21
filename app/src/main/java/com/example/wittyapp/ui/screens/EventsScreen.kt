package com.example.wittyapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.wittyapp.ui.DonkiEvent
import com.example.wittyapp.ui.SpaceWeatherViewModel

@Composable
fun EventsScreen(vm: SpaceWeatherViewModel) {
    val events = vm.state.events

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("События (DONKI)", style = MaterialTheme.typography.headlineMedium, color = Color.White)

        if (events.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))) {
                Text(
                    "Пока нет событий (или они не загрузились).",
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }
        } else {
            events.forEach { e -> EventCard(e) }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "тут был Женя",
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun EventCard(e: DonkiEvent) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("${e.type}: ${e.title}", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(e.timeTag, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            e.note?.let {
                Text(it, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}