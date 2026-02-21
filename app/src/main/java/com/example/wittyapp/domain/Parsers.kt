package com.example.wittyapp.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

// ===== Models for 1-minute feeds =====

data class KpSample(
    val t: Instant,
    val kp: Double
)

data class WindSample(
    val t: Instant,
    val speed: Double,
    val density: Double?
)

data class MagSample(
    val t: Instant,
    val bx: Double?,
    val bz: Double?,
    val bt: Double?
)

// ===== Public API used by ViewModel =====

/**
 * NOAA SWPC Kp 1-minute (пример по твоему скрину):
 * [
 *  {"time_tag":"2026-02-21T09:28:00","kp_index":2,"estimated_kp":1.67,"kp":"2M"},
 *  ...
 * ]
 */
fun parseKp1m(body: String): List<KpSample> {
    val arr = parseRootArray(body) ?: return emptyList()
    return arr.mapNotNull { e ->
        val o = e.asObj() ?: return@mapNotNull null
        val t = o["time_tag"].asString()?.let(::parseInstantSafe) ?: return@mapNotNull null

        // Берём estimated_kp (плавное), если нет — kp_index (целое)
        val kp = o["estimated_kp"].asDouble()
            ?: o["kp_index"].asDouble()
            ?: return@mapNotNull null

        KpSample(t = t, kp = kp)
    }.sortedBy { it.t }
}

/**
 * NOAA SWPC Solar Wind 1-minute (пример по твоему скрину):
 * [
 *  {"time_tag":"2026-02-21T15:25:00","active":true,"source":"ACE",
 *   "proton_speed":492.6,"proton_density":6.30, ...},
 *  ...
 * ]
 */
fun parseWind1m(body: String): List<WindSample> {
    val arr = parseRootArray(body) ?: return emptyList()
    return arr.mapNotNull { e ->
        val o = e.asObj() ?: return@mapNotNull null
        val t = o["time_tag"].asString()?.let(::parseInstantSafe) ?: return@mapNotNull null

        val speed = o["proton_speed"].asDouble() ?: return@mapNotNull null
        val density = o["proton_density"].asDouble()

        WindSample(t = t, speed = speed, density = density)
    }.sortedBy { it.t }
}

/**
 * NOAA SWPC MAG 1-minute (пример по твоему скрину):
 * [
 *  {"time_tag":"2026-02-21T15:24:00","bt":11.77,"bx_gse":4.15,"bz_gse":10.04,
 *   "bx_gsm":4.12,"bz_gsm":8.29, ...},
 *  ...
 * ]
 *
 * Для компаса логичнее брать GSM (если есть), иначе GSE.
 */
fun parseMag1m(body: String): List<MagSample> {
    val arr = parseRootArray(body) ?: return emptyList()
    return arr.mapNotNull { e ->
        val o = e.asObj() ?: return@mapNotNull null
        val t = o["time_tag"].asString()?.let(::parseInstantSafe) ?: return@mapNotNull null

        val bt = o["bt"].asDouble()

        val bx = o["bx_gsm"].asDouble() ?: o["bx_gse"].asDouble()
        val bz = o["bz_gsm"].asDouble() ?: o["bz_gse"].asDouble()

        MagSample(t = t, bx = bx, bz = bz, bt = bt)
    }.sortedBy { it.t }
}

// ===== Helpers =====

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

/**
 * Поддержка:
 * - корень = JSON Array (как у тебя сейчас)
 * - корень = JSON Object с полем data/result/values (на будущее)
 */
private fun parseRootArray(body: String): JsonArray? {
    val el = runCatching { json.parseToJsonElement(body) }.getOrNull() ?: return null
    return when (el) {
        is JsonArray -> el
        is JsonObject -> {
            // fallback: если когда-то источник станет объектом
            el["data"]?.asArr()
                ?: el["result"]?.asArr()
                ?: el["values"]?.asArr()
        }
        else -> null
    }
}

private fun JsonElement.asObj(): JsonObject? = (this as? JsonObject)
private fun JsonElement.asArr(): JsonArray? = (this as? JsonArray)

private fun JsonElement?.asString(): String? {
    val p = this as? JsonPrimitive ?: return null
    if (p is JsonNull) return null
    return runCatching { p.content }.getOrNull()
}

private fun JsonElement?.asDouble(): Double? {
    val p = this as? JsonPrimitive ?: return null
    if (p is JsonNull) return null
    // В этих фидах числа могут быть как number или как string
    return p.doubleOrNullSafe()
}

private fun JsonPrimitive.doubleOrNullSafe(): Double? {
    // contentOrNull/doubleOrNull иногда отсутствуют в старых заготовках — делаем вручную
    val raw = runCatching { this.content }.getOrNull() ?: return null
    return raw.toDoubleOrNull()
}

private fun parseInstantSafe(s: String): Instant =
    runCatching { Instant.parse(s) }.getOrElse { Instant.now() }