package com.example.wittyapp.domain

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class KpNow(val timeTag: String, val kp: Double)

fun parseKpNow(raw: JsonElement): KpNow? {
    val arr = raw as? JsonArray ?: return null
    if (arr.size < 2) return null

    val last = arr.lastOrNull() as? JsonArray ?: return null
    val time = last.getOrNull(0)?.jsonPrimitive?.contentOrNull ?: return null
    val kp = last.getOrNull(1)?.jsonPrimitive?.doubleOrNull ?: return null

    return KpNow(timeTag = time, kp = kp)
}

fun parsePlasmaNow(raw: JsonElement): Pair<String, Pair<Double?, Double?>>? {
    val arr = raw as? JsonArray ?: return null
    if (arr.size < 2) return null

    val last = arr.lastOrNull() as? JsonArray ?: return null
    val time = last.getOrNull(0)?.jsonPrimitive?.contentOrNull ?: return null

    val density = last.getOrNull(1)?.jsonPrimitive?.doubleOrNull
    val speed = last.getOrNull(2)?.jsonPrimitive?.doubleOrNull

    return time to (speed to density)
}

fun parseMagBzNow(raw: JsonElement): Pair<String, Double?>? {
    val arr = raw as? JsonArray ?: return null
    if (arr.size < 2) return null

    val last = arr.lastOrNull() as? JsonArray ?: return null
    val time = last.getOrNull(0)?.jsonPrimitive?.contentOrNull ?: return null

    val bz = last.getOrNull(3)?.jsonPrimitive?.doubleOrNull
    return time to bz
}