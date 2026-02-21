package com.example.wittyapp.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

private val JSON = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * These parsers are defensive:
 * - if API returns JsonObject instead of JsonArray, we try to find array inside ("data"/"values"/...)
 * - if formats differ, we try multiple common field names
 *
 * Return types are intentionally simple to avoid dependency on reflection.
 */
object Parsers {

    /** Kp "now" single value (best effort) */
    fun parseKpNow(raw: String): Double? {
        val root = JSON.parseToJsonElement(raw)
        // case 1: object with direct fields
        JsonSafe.obj(root)?.let { o ->
            JsonSafe.dbl(o, "kp")?.let { return it }
            JsonSafe.dbl(o, "kp_index")?.let { return it }
            JsonSafe.dbl(o, "value")?.let { return it }
        }
        // case 2: array -> take last row
        val arr = JsonSafe.asArrayOrNull(root) ?: return null
        val last = arr.lastOrNull() ?: return null
        return extractDoubleFromRow(last, keys = listOf("kp", "kp_index", "value", "Kp"))
    }

    /** Solar wind "now": speed + density */
    fun parsePlasmaNow(raw: String): Pair<Double?, Double?> {
        val root = JSON.parseToJsonElement(raw)
        JsonSafe.obj(root)?.let { o ->
            val speed = JsonSafe.dbl(o, "speed") ?: JsonSafe.dbl(o, "wind_speed") ?: JsonSafe.dbl(o, "v")
            val dens = JsonSafe.dbl(o, "density") ?: JsonSafe.dbl(o, "rho") ?: JsonSafe.dbl(o, "n")
            if (speed != null || dens != null) return speed to dens
        }
        val arr = JsonSafe.asArrayOrNull(root) ?: return null to null
        val last = arr.lastOrNull() ?: return null to null
        val speed = extractDoubleFromRow(last, listOf("speed", "wind_speed", "v", "value"))
        val dens = extractDoubleFromRow(last, listOf("density", "rho", "n"))
        return speed to dens
    }

    /** Magnetometer "now": bz + bx (best effort) */
    fun parseMagNow(raw: String): Pair<Double?, Double?> {
        val root = JSON.parseToJsonElement(raw)
        JsonSafe.obj(root)?.let { o ->
            val bz = JsonSafe.dbl(o, "bz") ?: JsonSafe.dbl(o, "Bz") ?: JsonSafe.dbl(o, "btz")
            val bx = JsonSafe.dbl(o, "bx") ?: JsonSafe.dbl(o, "Bx") ?: JsonSafe.dbl(o, "btx")
            if (bz != null || bx != null) return bz to bx
        }
        val arr = JsonSafe.asArrayOrNull(root) ?: return null to null
        val last = arr.lastOrNull() ?: return null to null
        val bz = extractDoubleFromRow(last, listOf("bz", "Bz", "btz", "z"))
        val bx = extractDoubleFromRow(last, listOf("bx", "Bx", "btx", "x"))
        return bz to bx
    }

    /**
     * 24h series parsers.
     * Return: list of pairs (timeLabel, value) already prepared for graph builder if needed.
     */
    fun parseSeries24h(raw: String, valueKeys: List<String>): List<Pair<Instant, Double>> {
        val root = JSON.parseToJsonElement(raw)
        val arr = JsonSafe.asArrayOrNull(root) ?: return emptyList()

        val out = mutableListOf<Pair<Instant, Double>>()
        for (row in arr) {
            val t = extractInstantFromRow(row) ?: continue
            val v = extractDoubleFromRow(row, valueKeys) ?: continue
            out += t to v
        }
        return out
    }

    // ---- internal helpers ----

    private fun extractInstantFromRow(row: JsonElement): Instant? {
        // row can be array ["2024-..", "123"] OR object {"time_tag": "...", "value": ...}
        return when {
            isArrayRow(row) -> {
                val a = row.jsonArray
                val t0 = a.getOrNull(0)?.jsonPrimitive?.contentOrNull()
                parseInstant(t0)
            }
            else -> {
                val o = row.jsonObjectOrNull() ?: return null
                val candidates = listOf("time_tag", "time", "timestamp", "date", "datetime", "t")
                for (k in candidates) {
                    val s = o[k]?.jsonPrimitive?.contentOrNull()
                    parseInstant(s)?.let { return it }
                }
                // sometimes epoch millis in "timestamp"
                val ms = candidates.asSequence()
                    .mapNotNull { o[it]?.jsonPrimitive?.contentOrNull()?.toLongOrNull() }
                    .firstOrNull()
                if (ms != null) Instant.ofEpochMilli(ms) else null
            }
        }
    }

    private fun extractDoubleFromRow(row: JsonElement, keys: List<String>): Double? {
        return when {
            isArrayRow(row) -> {
                val a = row.jsonArray
                // usually [time, value] -> index 1
                a.getOrNull(1)?.jsonPrimitive?.contentOrNull()?.toDoubleOrNull()
            }
            else -> {
                val o = row.jsonObjectOrNull() ?: return null
                for (k in keys) {
                    o[k]?.jsonPrimitive?.contentOrNull()?.toDoubleOrNull()?.let { return it }
                }
                null
            }
        }
    }

    private fun isArrayRow(e: JsonElement): Boolean =
        runCatching { e.jsonArray; true }.getOrDefault(false)

    private fun JsonElement.jsonObjectOrNull() =
        runCatching { this.jsonObject }.getOrNull()

    private fun kotlinx.serialization.json.JsonPrimitive.contentOrNull(): String? =
        runCatching { this.content }.getOrNull()

    private fun parseInstant(s: String?): Instant? {
        if (s.isNullOrBlank()) return null
        // try ISO instant first
        runCatching { return Instant.parse(s) }.getOrNull()
        // try epoch seconds / millis
        val n = s.toLongOrNull()
        if (n != null) {
            // heuristic: millis if too large
            return if (n > 3_000_000_000L) Instant.ofEpochMilli(n) else Instant.ofEpochSecond(n)
        }
        return null
    }
}