package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.EventsScreen
import com.example.wittyapp.ui.screens.GraphsScreen
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = SpaceWeatherApi(nasaApiKey = "DEMO_KEY")

        setContent {
            val vm: SpaceWeatherViewModel =
                viewModel(factory = SimpleFactory { SpaceWeatherViewModel(api) })

            var screen by remember { mutableStateOf(Screen.NOW) }

            CosmosTheme(auroraScore = vm.state.auroraScore) {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = screen == Screen.NOW,
                                onClick = { screen = Screen.NOW },
                                icon = { Icon(Icons.Default.WbSunny, contentDescription = null) },
                                label = { Text("Сейчас") }
                            )
                            NavigationBarItem(
                                selected = screen == Screen.GRAPHS,
                                onClick = { screen = Screen.GRAPHS },
                                icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                                label = { Text("Графики") }
                            )
                            NavigationBarItem(
                                selected = screen == Screen.EVENTS,
                                onClick = { screen = Screen.EVENTS },
                                icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                label = { Text("События") }
                            )
                        }
                    }
                ) { pad ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(Modifier)
                    ) {
                        // AnimatedContent даст плавные переходы без navigation-compose
                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = {
                                fadeIn(tween(220)) togetherWith fadeOut(tween(220))
                            },
                            label = "screen"
                        ) { s ->
                            when (s) {
                                Screen.NOW -> NowScreen(
                                    vm = vm,
                                    onOpenGraphs = { screen = Screen.GRAPHS }
                                )
                                Screen.GRAPHS -> GraphsScreen(
                                    title = "Графики (24ч)",
                                    series = vm.simpleGraphSeries(),
                                    onClose = { screen = Screen.NOW }
                                )
                                Screen.EVENTS -> EventsScreen(vm)
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class Screen { NOW, GRAPHS, EVENTS }

private class SimpleFactory<T : androidx.lifecycle.ViewModel>(
    val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <R : androidx.lifecycle.ViewModel> create(modelClass: Class<R>): R {
        @Suppress("UNCHECKED_CAST")
        return creator() as R
    }
}