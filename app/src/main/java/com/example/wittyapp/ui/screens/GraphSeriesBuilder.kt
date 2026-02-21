package com.example.wittyapp.ui.screens

import com.example.wittyapp.domain.GraphPoint
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun buildUiSeries(
    title: String,
    unit: String,
    points: List<GraphPoint>,
    minY: Double,
    maxY: Double,
    gridStep: Double
): GraphSeries {

    val formatter = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())

    val uiPoints = points.map {
        UiGraphPoint(
            xLabel = formatter.format(it.t),
            value = it.value
        )
    }

    return GraphSeries(
        title = title,
        unit = unit,
        points = uiPoints,
        minY = minY,
        maxY = maxY,
        gridStepY = gridStep
    )
}