package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.*
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppRoot()
        }
    }
}

@Composable
private fun AppRoot() {

    val api = remember { SpaceWeatherApi() }
    val vm: SpaceWeatherViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SpaceWeatherViewModel(api) as T
            }
        }
    )

    var currentScreen by remember { mutableStateOf("now") }
    var mode by remember { mutableStateOf(AppMode.EARTH) }

    CosmosTheme(
        mode = mode,
        auroraScore = vm.state.auroraScore
    ) {

        androidx.compose.material3.Scaffold { padding ->

            when (currentScreen) {

                "now" -> NowScreen(
                    vm = vm,
                    mode = mode,
                    strings = com.example.wittyapp.ui.strings.AppStrings.ru(),
                    contentPadding = padding,
                    onOpenGraphs = { currentScreen = "graphs" },
                    onOpenEvents = {}
                )

                "graphs" -> {

                    val series = buildUiSeriesFromState(vm)

                    GraphsScreen(
                        title = "Графики 24ч",
                        series = series,
                        mode = if (mode == AppMode.EARTH)
                            GraphsMode.EARTH
                        else
                            GraphsMode.SUN,
                        strings = com.example.wittyapp.ui.strings.AppStrings.ru(),
                        onClose = { currentScreen = "now" }
                    )
                }
            }
        }
    }
}