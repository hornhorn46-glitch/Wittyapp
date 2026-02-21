package com.example.wittyapp

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = SpaceWeatherApi(nasaApiKey = "DEMO_KEY")

        setContent {
            val vm: SpaceWeatherViewModel =
                viewModel(factory = SimpleFactory { SpaceWeatherViewModel(api) })

            // Стек экранов (инфраструктура "назад")
            var stack by remember { mutableStateOf(listOf(Screen.NOW)) }

            fun push(s: Screen) { stack = stack + s }
            fun pop(): Boolean {
                return if (stack.size > 1) {
                    stack = stack.dropLast(1)
                    true
                } else false
            }
            fun setRoot(s: Screen) { stack = listOf(s) }

            val current = stack.last()

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var lastBackAt by remember { mutableStateOf(0L) }

            BackHandler {
                // 1) если есть куда вернуться — возвращаемся
                if (pop()) return@BackHandler

                // 2) иначе — двойной назад для выхода
                val now = SystemClock.elapsedRealtime()
                if (now - lastBackAt < 1800L) {
                    finish()
                } else {
                    lastBackAt = now
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Нажмите НАЗАД ещё раз для выхода",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            CosmosTheme(auroraScore = vm.state.auroraScore) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = current == Screen.NOW,
                                onClick = { setRoot(Screen.NOW) },
                                icon = { Icon(Icons.Default.WbSunny, contentDescription = null) },
                                label = { Text("Сейчас") }
                            )
                            NavigationBarItem(
                                selected = current == Screen.GRAPHS,
                                onClick = { setRoot(Screen.GRAPHS) },
                                icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                                label = { Text("Графики") }
                            )
                            NavigationBarItem(
                                selected = current == Screen.EVENTS,
                                onClick = { setRoot(Screen.EVENTS) },
                                icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                label = { Text("События") }
                            )
                        }
                    }
                ) { _ ->
                    AnimatedContent(
                        targetState = current,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        },
                        label = "screen"
                    ) { s ->
                        when (s) {
                            Screen.NOW -> NowScreen(
                                vm = vm,
                                onOpenGraphs = { setRoot(Screen.GRAPHS) }
                            )
                            Screen.GRAPHS -> GraphsScreen(
                                title = "Графики (24ч)",
                                series = vm.simpleGraphSeries(),
                                onClose = { setRoot(Screen.NOW) }
                            )
                            Screen.EVENTS -> EventsScreen(vm)
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