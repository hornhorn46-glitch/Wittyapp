package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.AppTabs
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.AuroraScreen
import com.example.wittyapp.ui.screens.EventsScreen
import com.example.wittyapp.ui.screens.NowScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = SpaceWeatherApi(nasaApiKey = "DEMO_KEY")

        setContent {
            MaterialTheme {
                val vm: SpaceWeatherViewModel = viewModel(factory = SimpleFactory { SpaceWeatherViewModel(api) })

                LaunchedEffect(Unit) {
                    vm.startAutoRefresh()
                }

                AppTabs(
                    now = { NowScreen(vm) },
                    aurora = { AuroraScreen(vm) },
                    events = { EventsScreen(vm) }
                )
            }
        }
    }
}

/**
 * Мини-фабрика без DI, чтобы не тащить лишнее в v2.
 */
private class SimpleFactory<T : androidx.lifecycle.ViewModel>(
    val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <R : androidx.lifecycle.ViewModel> create(modelClass: Class<R>): R {
        @Suppress("UNCHECKED_CAST")
        return creator() as R
    }
}