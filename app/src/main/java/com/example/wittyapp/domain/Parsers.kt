package com.example.wittyapp.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

private val JSON = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Parsers for various space weather endpoints.
 * Important: this file provides legacy function names used by SpaceWeatherViewModel:
 *  - parseKp1m
 *  - parseWind1m
 *  - parseMag1m
 *
 * And also "now" helpers:
 *  - parseKpNow
 *  - parsePlasmaNow
 *  - parseMagNow
 */
object Parsers {

    /* ---------------- Legacy API expected by ViewModel ---------------- */

    fun parseKp1m(raw: String): Double? = parseKpNow(raw)

    /** Returns Pair(speed, density) */
    fun parseWind1m(raw: String): Pair<Double?, Double?> = parsePlasmaNow(raw)

    /** Returns Pair(bz, bx) */
    fun parseMag1m(raw: String): Pair<Double?, Double?> = parseMagNow(raw)

    /* ---------------- Current helpers ---------------- */

    /** Kp "now" single value (best effort) */
    fun parseKpNow(raw: String): Double? {
        val root = JSON.parseToJsonElement(raw)

        // case 1: object with direct fields
        obj(root)?.let { o ->
            dbl(o, "kp")?.let { return it }
            dbl(o, "kp_index")?.let { return it }
            dbl(o, "value")?.let { return it }
        }

        // case 2: array -> take last row
        val arr = asArrayOrNull(root) ?: return null
        val last = arr.lastOrNull() ?: return null
        return extractDoubleFromRow(last, keys = listOf("kp", "kp_index", "value", "Kp"))
    }

    /** Solar wind "now": speed + density */
    fun parsePlasmaNow(raw: String): Pair<Double?, Double?> {
        val root = JSON.parseToJsonElement(raw)

        obj(root)?.let { o ->
            val speed = dbl(o, "speed") ?: dbl(o, "wind_speed") ?: dbl(o, "v")
            val dens = dbl(o, "density") ?: dbl(o, "rho") ?: dbl(o, "n")
            if (speed != null || dens != null) return speed to dens
        }

        val arr = asArrayOrNull(root) ?: return null to null
        val last = arr.lastOrNull() ?: return null to null
        val speed = extractDoubleFromRow(last, listOf("speed", "wind_speed", "v", "value"))
        val dens = extractDoubleFromRow(last, listOf("density", "rho", "n"))
        return speed to dens
    }

    /** Magnetometer "now": bz + bx (best effort) */
    fun parseMagNow(raw: String): Pair<Double?, Double?> {
        val root = JSON.parseToJsonElement(raw)

        obj(root)?.let { o ->
            val bz = dbl(o, "bz") ?: dbl(o, "Bz") ?: dbl(o, "btz") ?: dbl(o, "z")
            val bx = dbl(o, "bx") ?: dbl(o, "Bx") ?: dbl(o, "btx") ?: dbl(o, "x")
            if (bz != null || bx != null) return bz to bx
        }

        val arr = asArrayOrNull(root) ?: return null to null
        val last = arr.lastOrNull() ?: return null to null
        val bz = extractDoubleFromRow(last, listOf("bz", "Bz", "btz", "z"))
        val bx = extractDoubleFromRow(last, listOf("bx", "Bx", "btx", "x"))
        return bz to bx
    }

    /**
     * 24h series parser.
     * Return: list of pairs (Instant, value).
     */
    fun parseSeries24h(raw: String, valueKeys: List<String>): List<Pair<Instant, Double>> {
        val root = JSON.parseToJsonElement(raw)
        val arr = asArrayOrNull(root) ?: return emptyList()

        val out = mutableListOf<Pair<Instant, Double>>()
        for (row in arr) {
            val t = extractInstantFromRow(row) ?: continue
            val v = extractDoubleFromRow(row, valueKeys) ?: continue
            out += t to v
        }
        return out
    }

    /* ---------------- Robust JSON wrappers ---------------- */

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
                // nested
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

    private fun obj(root: JsonElement?): JsonObject? =
        runCatching { root?.jsonObject }.getOrNull()

    private fun dbl(obj: JsonObject?, key: String): Double? =
        runCatching { obj?.get(key)?.jsonPrimitive?.content?.toDoubleOrNull() }.getOrNull()

    /* ---------------- row extraction ---------------- */

    private fun extractInstantFromRow(row: JsonElement): Instant? {
        // array row: [time, value]
        if (isArrayRow(row)) {
            val a = row.jsonArray
            val t0 = a.getOrNull(0)?.jsonPrimitive?.contentOrNull()
            return parseInstant(t0)
        }

        val o = row.jsonObjectOrNull() ?: return null
        val candidates = listOf("time_tag", "time", "timestamp", "date", "datetime", "t")
        for (k in candidates) {
            val s = o[k]?.jsonPrimitive?.contentOrNull()
            parseInstant(s)?.let { return it }
        }
        val ms = candidates.asSequence()
            .mapNotNull { o[it]?.jsonPrimitive?.contentOrNull()?.toLongOrNull() }
            .firstOrNull()
        return if (ms != null) Instant.ofEpochMilli(ms) else null
    }

    private fun extractDoubleFromRow(row: JsonElement, keys: List<String>): Double? {
        if (isArrayRow(row)) {
            val a = row.jsonArray
            return a.getOrNull(1)?.jsonPrimitive?.contentOrNull()?.toDoubleOrNull()
        }
        val o = row.jsonObjectOrNull() ?: return null
        for (k in keys) {
            o[k]?.jsonPrimitive?.contentOrNull()?.toDoubleOrNull()?.let { return it }
        }
        return null
    }

    private fun isArrayRow(e: JsonElement): Boolean =
        runCatching { e.jsonArray; true }.getOrDefault(false)

    private fun JsonElement.jsonObjectOrNull() =
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
}