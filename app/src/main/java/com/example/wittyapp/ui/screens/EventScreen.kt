package com.example.wittyapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.wittyapp.net.SpaceWeatherApi

@Composable
fun EventsScreen(api: SpaceWeatherApi) {
    Column {
        Text("События (DONKI) — скоро")
    }
}