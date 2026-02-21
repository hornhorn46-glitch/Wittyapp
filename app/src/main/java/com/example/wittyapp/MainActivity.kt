package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
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

                when (screen) {

                    Screen.NOW -> NowScreen(
                        vm = vm,
                        onOpenGraphs = { screen = Screen.GRAPHS }
                    )

                    Screen.GRAPHS -> GraphsScreen(
                        title = "Графики",
                        series = vm.simpleGraphSeries(),
                        onClose = { screen = Screen.NOW }
                    )
                }
            }
        }
    }
}

private enum class Screen { NOW, GRAPHS }

private class SimpleFactory<T : androidx.lifecycle.ViewModel>(
    val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <R : androidx.lifecycle.ViewModel> create(modelClass: Class<R>): R {
        @Suppress("UNCHECKED_CAST")
        return creator() as R
    }
}