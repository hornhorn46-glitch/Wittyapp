package com.example.wittyapp.ui.screens

import com.example.wittyapp.ui.SpaceWeatherViewModel
import kotlin.math.roundToInt

fun buildUiSeriesFromState(vm: SpaceWeatherViewModel): List<GraphSeries> {

    return listOf(
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
}