package com.example.wittyapp.domain

import kotlinx.serialization.json.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class KpSample(val time: Instant, val kp: Double)
data class PlasmaSample(val time: Instant, val speedKmS: Double?, val densityCC: Double?)
data class MagSample(val time: Instant, val bzNt: Double?)

data class GraphPoint(val x: Float, val y: Float)

fun parseKpSeries(raw: JsonElement): List<KpSample> {
    val arr = raw as? JsonArray ?: return emptyList()
    if (arr.size < 2) return emptyList()
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
        val bz = r.getOrNull(3)?.jsonPrimitive?.doubleOrNull
        MagSample(time, bzNt = bz)
    }
}

/** Берём последние 24ч и делаем точки "минуты назад" -> value */
fun kpToGraph24h(series: List<KpSample>, now: Instant): List<GraphPoint> {
    val from = now.minusSeconds(24 * 3600)
    val s = series.filter { it.time >= from && it.time <= now }
    return s.map { GraphPoint(x = ((it.time.epochSecond - from.epochSecond) / 60f), y = it.kp.toFloat()) }
}

fun bzToGraph24h(series: List<MagSample>, now: Instant): List<GraphPoint> {
    val from = now.minusSeconds(24 * 3600)
    val s = series.filter { it.time >= from && it.time <= now }.mapNotNull { it.bzNt?.let { bz -> it.time to bz } }
    return s.map { (t, bz) -> GraphPoint(x = ((t.epochSecond - from.epochSecond) / 60f), y = bz.toFloat()) }
}

fun speedToGraph24h(series: List<PlasmaSample>, now: Instant): List<GraphPoint> {
    val from = now.minusSeconds(24 * 3600)
    val s = series.filter { it.time >= from && it.time <= now }.mapNotNull { it.speedKmS?.let { v -> it.time to v } }
    return s.map { (t, v) -> GraphPoint(x = ((t.epochSecond - from.epochSecond) / 60f), y = v.toFloat()) }
}

private fun parseSwpcTime(s: String): Instant? {
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