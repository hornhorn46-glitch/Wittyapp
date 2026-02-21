package com.example.wittyapp.ui.screens

import androidx.compose.runtime.Composable
import com.example.wittyapp.ui.SpaceWeatherViewModel

/**
 * Legacy file: оставляем для совместимости, но меняем имя функции,
 * чтобы не конфликтовать с EventsScreen.kt
 */
@Composable
fun EventScreen(vm: SpaceWeatherViewModel) {
    EventsScreen(vm)
}