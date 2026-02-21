package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.AppTabs
import com.example.wittyapp.ui.screens.AuroraScreen
import com.example.wittyapp.ui.screens.EventsScreen
import com.example.wittyapp.ui.screens.NowScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = SpaceWeatherApi(nasaApiKey = "DEMO_KEY")

        setContent {
            MaterialTheme {
                AppTabs(
                    now = { NowScreen(api) },
                    aurora = { AuroraScreen() },
                    events = { EventsScreen(api) }
                )
            }
        }
    }
}