package com.example.wittyapp.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.GraphsScreen
import com.example.wittyapp.ui.screens.NowScreen

@Composable
fun AppNavHost(vm: SpaceWeatherViewModel) {

    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = "now"
    ) {

        composable(
            route = "now",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            NowScreen(
                vm = vm,
                onOpenGraphs = { nav.navigate("graphs") }
            )
        }

        composable(
            route = "graphs",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            GraphsScreen(
                title = "Графики",
                series = vm.buildGraphSeries(),
                onClose = { nav.popBackStack() }
            )
        }
    }
}