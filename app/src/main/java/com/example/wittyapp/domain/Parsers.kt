package com.example.wittyapp.domain

import kotlinx.serialization.json.*

data class KpNow(val timeTag: String, val kp: Double)

data class SolarWindNow(
    val timeTag: String,
    val speedKmS: Double?,   // V
    val densityCC: Double?,  // n
    val bzNt: Double?        // Bz
)

/**
 * noaa-planetary-k-index.json:
 * JSON-массив: первая строка — заголовки, дальше строки данных.
 * Берём последнюю строку и достаём time_tag + kp.
 */
fun parseKpNow(raw: JsonElement): KpNow? {
    val arr = raw as? JsonArray ?: return null