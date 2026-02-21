package com.example.wittyapp.domain

import kotlinx.serialization.json.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class KpSample(val time: Instant, val kp: Double)
data class PlasmaSample(val time: Instant, val speedKmS: Double?, val densityCC: Double?)
data class MagSample(val time: Instant, val bzNt: Double?)

fun parseKpSeries(raw: JsonElement): List<KpSample> {
    val arr = raw as? JsonArray ?: return emptyList()
    if (arr.size < 2) return emptyList()

    // Пропускаем header
    return arr.drop(1).mapNotNull { row ->
        val r = row as? JsonArray ?: return@mapNotNull null
        val time = r.getOrNull(0)?.jsonPrimitive?.contentOrNull?.let(::parseSwpcTime) ?: return@mapNotNull null
        val kp = r.getOrNull(1)?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null
        KpSample(time, kp)
    }
}

fun parsePlasmaSeries(raw: JsonElement): List<PlasmaSample> {
    val arr = raw as? JsonArray ?: return emptyList()
    if (arr.size < 2) return emptyList()

    return arr.drop(1).mapNotNull { row ->
        val r = row as? JsonArray ?: return@mapNotNull null
        val time = r.getOrNull(0)?.jsonPrimitive?.contentOrNull?.let(::parseSwpcTime) ?: return@mapNotNull null

        // Обычно: [time_tag, density, speed, ...]
        val density = r.getOrNull(1)?.jsonPrimitive?.doubleOrNull
        val speed = r.getOrNull(2)?.jsonPrimitive?.doubleOrNull

        PlasmaSample(time, speedKmS = speed, densityCC = density)
    }
}

fun parseMagSeries(raw: JsonElement): List<MagSample> {
    val arr = raw as? JsonArray ?: return emptyList()
    if (arr.size < 2) return emptyList()

    return arr.drop(1).mapNotNull { row ->
        val r = row as? JsonArray ?: return@mapNotNull null
        val time = r.getOrNull(0)?.jsonPrimitive?.contentOrNull?.let(::parseSwpcTime) ?: return@mapNotNull null

        // Часто: [time_tag, bx, by, bz, bt...]
        val bz = r.getOrNull(3)?.jsonPrimitive?.doubleOrNull
        MagSample(time, bzNt = bz)
    }
}

/**
 * SWPC часто отдаёт time_tag как:
 * "yyyy-MM-dd HH:mm:ss.SSS" или "yyyy-MM-dd HH:mm:ss"
 * Иногда уже ISO-8601.
 * Для простоты считаем время как UTC.
 */
private fun parseSwpcTime(s: String): Instant? {
    // ISO
    runCatching { return Instant.parse(s) }.getOrNull()

    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (p in patterns) {
        val fmt = DateTimeFormatter.ofPattern(p)
        val dt = runCatching { LocalDateTime.parse(s, fmt) }.getOrNull()
        if (dt != null) return dt.toInstant(ZoneOffset.UTC)
    }
    return null
}