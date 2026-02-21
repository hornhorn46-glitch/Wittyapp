package com.example.wittyapp.domain

import kotlinx.serialization.json.*

fun parseKpNow(json: String): Double? {
    val arr = Json.parseToJsonElement(json).jsonArray
    return arr.lastOrNull()?.jsonArray?.getOrNull(1)?.jsonPrimitive?.doubleOrNull
}

fun parsePlasmaNow(json: String): Triple<Double?, Double?, Double?> {
    val arr = Json.parseToJsonElement(json).jsonArray
    val last = arr.lastOrNull()?.jsonArray ?: return Triple(null, null, null)

    val speed = last.getOrNull(1)?.jsonPrimitive?.doubleOrNull
    val density = last.getOrNull(2)?.jsonPrimitive?.doubleOrNull
    val temp = last.getOrNull(3)?.jsonPrimitive?.doubleOrNull

    return Triple(speed, density, temp)
}

fun parseMagBzNow(json: String): Double? {
    val arr = Json.parseToJsonElement(json).jsonArray
    return arr.lastOrNull()?.jsonArray?.getOrNull(2)?.jsonPrimitive?.doubleOrNull
}