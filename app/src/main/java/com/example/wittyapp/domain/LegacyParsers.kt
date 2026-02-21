package com.example.wittyapp.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

private val JSON = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Top-level legacy parsers expected by SpaceWeatherViewModel:
 *  - parseKp1m
 *  - parseWind1m
 *  - parseMag1m
 *
 * They return domain models from Models.kt:
 *  KpSample, WindSample, MagSample
 */
fun parseKp1m(raw: String): List<KpSample> {
    val root = JSON.parseToJsonElement(raw)
    val arr = asArrayOrNull(root) ?: return emptyList()

    val out = ArrayList<KpSample>(arr.size)
    for (row in arr) {
        val t = extractInstant(row) ?: continue
        val kp = extractDouble(row, keys = listOf("kp", "kp_index", "Kp", "value")) ?: continue
        out += KpSample(t = t, kp = kp)
    }
    return out.sortedBy { it.t }
}

fun parseWind1m(raw: String): List<WindSample> {
    val root = JSON.parseToJsonElement(raw)
    val arr = asArrayOrNull(root) ?: return emptyList()

    val out = ArrayList<WindSample>(arr.size)
    for (row in arr) {
        val t = extractInstant(row) ?: continue

        // speed is mandatory, density optional
        val speed = extractDouble(row, keys = listOf("speed", "wind_speed", "v", "value")) ?: continue
        val density = extractDouble(row, keys = listOf("density", "rho", "n"))

        out += WindSample(t = t, speed = speed, density = density)
    }
    return out.sortedBy { it.t }
}

fun parseMag1m(raw: String): List<MagSample> {
    val root = JSON.parseToJsonElement(raw)
    val arr = asArrayOrNull(root) ?: return emptyList()

    val out = ArrayList<MagSample>(arr.size)
    for (row in arr) {
        val t = extractInstant(row) ?: continue

        // bx/by/bz are optional (some feeds omit parts)
        val bx = extractDouble(row, keys = listOf("bx", "Bx", "btx", "x"))
        val by = extractDouble(row, keys = listOf("by", "By", "bty", "y"))
        val bz = extractDouble(row, keys = listOf("bz", "Bz", "btz", "z"))

        out += MagSample(t = t, bx = bx, by = by, bz = bz)
    }
    return out.sortedBy { it.t }
}

/* ---------------- helpers ---------------- */

private fun asArrayOrNull(root: JsonElement?): JsonArray? {
    if (root == null || root is JsonNull) return null
    return when (root) {
        is JsonArray -> root
        is JsonObject -> {
            val keys = listOf(
                "data", "values", "result", "records", "items", "list",
                "Data", "Values", "Result"
            )
            for (k in keys) {
                val v = root[k]
                if (v is JsonArray) return v
            }
            for (k in keys) {
                val v = root[k]
                if (v is JsonObject) {
                    for (k2 in keys) {
                        val v2 = v[k2]
                        if (v2 is JsonArray) return v2
                    }
                }
            }
            null
        }
        else -> null
    }
}

private fun extractInstant(row: JsonElement): Instant? {
    // array row: [time, value] or [time, ...]
    if (isArrayRow(row)) {
        val a = row.jsonArray
        val t0 = a.getOrNull(0)?.jsonPrimitive?.contentOrNull()
        return parseInstant(t0)
    }

    // object row
    val o = row.jsonObjectOrNull() ?: return null
    val candidates = listOf("time_tag", "time", "timestamp", "date", "datetime", "t")
    for (k in candidates) {
        val s = o[k]?.jsonPrimitive?.contentOrNull()
        parseInstant(s)?.let { return it }
    }

    // epoch (ms or sec) heuristic
    val n = candidates.asSequence()
        .mapNotNull { o[it]?.jsonPrimitive?.contentOrNull()?.toLongOrNull() }
        .firstOrNull()
    return if (n != null) {
        if (n > 3_000_000_000L) Instant.ofEpochMilli(n) else Instant.ofEpochSecond(n)
    } else null
}

private fun extractDouble(row: JsonElement, keys: List<String>): Double? {
    // array row: assume [time, value] => index 1
    if (isArrayRow(row)) {
        val a = row.jsonArray
        return a.getOrNull(1)?.jsonPrimitive?.contentOrNull()?.toDoubleOrNull()
    }

    // object row: try keys
    val o = row.jsonObjectOrNull() ?: return null
    for (k in keys) {
        o[k]?.jsonPrimitive?.contentOrNull()?.toDoubleOrNull()?.let { return it }
    }
    return null
}

private fun isArrayRow(e: JsonElement): Boolean =
    runCatching { e.jsonArray; true }.getOrDefault(false)

private fun JsonElement.jsonObjectOrNull(): JsonObject? =
    runCatching { this.jsonObject }.getOrNull()

private fun kotlinx.serialization.json.JsonPrimitive.contentOrNull(): String? =
    runCatching { this.content }.getOrNull()

private fun parseInstant(s: String?): Instant? {
    if (s.isNullOrBlank()) return null
    runCatching { return Instant.parse(s) }.getOrNull()

    val n = s.toLongOrNull()
    if (n != null) {
        return if (n > 3_000_000_000L) Instant.ofEpochMilli(n) else Instant.ofEpochSecond(n)
    }
    return null
}