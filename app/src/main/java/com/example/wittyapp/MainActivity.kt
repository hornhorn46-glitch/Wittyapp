package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Main entry point for the WittyApp. This activity uses Jetpack Compose to
 * display the current date and time along with a randomly selected witty
 * message. Pressing the "Обновить" button updates both the timestamp and
 * the message. The messages reflect a blend of programming, photography,
 * innovation, and forward‑thinking humour.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Use Material3 theme for a modern look and feel.
            MaterialTheme {
                WittyAppScreen()
            }
        }
    }
}

@Composable
fun WittyAppScreen() {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") }
    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var message by remember { mutableStateOf("Привет, мир!") }

    // A list of witty messages to display. Feel free to extend or customise.
    val messages = listOf(
        "Будущее — это сегодня, просто еще не все знают.",
        "Лучший способ предсказать будущее — создать его.",
        "Код, как и фотография, сохраняет момент навсегда.",
        "С каждым коммитом мы ближе к светлому завтра.",
        "Инновации начинаются с идеи… и заканчиваются тестированием."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Текущее время:",
            style = MaterialTheme.typography.titleLarge
        )
        // Display the formatted date and time.
        Text(
            text = dateTime.format(formatter),
            style = MaterialTheme.typography.titleMedium
        )
        // Display the current witty message.
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
        // Button to refresh the date/time and show a new message.
        Button(onClick = {
            dateTime = LocalDateTime.now()
            message = messages.random()
        }) {
            Text("Обновить")
        }
    }
}