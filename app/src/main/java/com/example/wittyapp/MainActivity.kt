package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.*
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = SpaceWeatherApi()

        setContent {

            var currentScreen by remember { mutableStateOf("now") }
            var mode by remember { mutableStateOf(AppMode.EARTH) }

            val strings = rememberAppStrings()

            val vm: SpaceWeatherViewModel = viewModel(
                factory = SpaceWeatherViewModelFactory(api)
            )

            CosmosTheme(mode = mode) {

                Scaffold { padding ->

                    when (currentScreen) {

                        "now" -> NowScreen(
                            vm = vm,
                            mode = mode,
                            strings = strings,
                            contentPadding = padding,
                            onOpenGraphs = { currentScreen = "graphs" },
                            onOpenEvents = { /* позже */ }
                        )

                        "graphs" -> {

                            val series = listOf(
                                buildUiSeries(
                                    title = "Kp",
                                    unit = "",
                                    points = vm.state.kpSeries24h,
                                    minY = 0.0,
                                    maxY = 9.0,
                                    gridStep = 1.0
                                ),
                                buildUiSeries(
                                    title = "Speed",
                                    unit = "км/с",
                                    points = vm.state.speedSeries24h,
                                    minY = 300.0,
                                    maxY = 1000.0,
                                    gridStep = 100.0
                                ),
                                buildUiSeries(
                                    title = "Bz",
                                    unit = "нТл",
                                    points = vm.state.bzSeries24h,
                                    minY = -20.0,
                                    maxY = 20.0,
                                    gridStep = 5.0
                                )
                            )

                            GraphsScreen(
                                title = "Графики 24ч",
                                series = series,
                                mode = if (mode == AppMode.EARTH)
                                    GraphsMode.EARTH
                                else
                                    GraphsMode.SUN,
                                strings = strings,
                                onClose = { currentScreen = "now" }
                            )
                        }
                    }
                }
            }
        }
    }
}