package com.example.wittyapp.ui.screens

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Build graph series list from vm.state without depending on a specific State class.
 * Uses Java reflection to probe common property/getter names.
 */
fun buildGraphSeries(state: Any): List<GraphSeries> {
    val out = mutableListOf<GraphSeries>()

    // Try to fetch lists from state using common names
    val kp = readDoubleSeries(state, listOf("kp24h", "kpHistory24h", "kpSeries24h", "kpSeries", "kpHistory"))
    val bz = readDoubleSeries(state, listOf("bz24h", "bzHistory24h", "bzSeries24h", "bzSeries", "bzHistory"))
    val bx = readDoubleSeries(state, listOf("bx24h", "bxHistory24h", "bxSeries24h", "bxSeries", "bxHistory"))
    val speed = readDoubleSeries(state, listOf("speed24h", "windSpeed24h", "speedHistory24h", "speedSeries24h", "speedSeries", "windSpeedSeries"))
    val density = readDoubleSeries(state, listOf("density24h", "rho24h", "densityHistory24h", "densitySeries24h", "densitySeries", "rhoSeries"))

    val labels = readTimeLabels(state, listOf("time24h", "timestamps24h", "times24h", "labels24h"))

    // Helper: build GraphPoint list with labels (if labels absent -> index labels)
    fun points(values: List<Double>, defaultPrefix: String): List<GraphPoint> {
        val n = values.size
        val labs = if (labels != null && labels.size == n) labels else null
        return List(n) { i ->
            val xl = labs?.get(i) ?: "$defaultPrefix${i + 1}"
            GraphPoint(xLabel = xl, value = values[i])
        }
    }

    // Kp (0..9, step 1)
    if (!kp.isNullOrEmpty()) {
        val pts = points(kp, "")
        out += GraphSeries(
            title = "Kp",
            unit = "",
            points = pts,
            minY = 0.0,
            maxY = 9.0,
            gridStepY = 1.0,
            dangerBelow = null,
            dangerAbove = 7.0
        )
    }

    // Bz (centered, step 2). Danger below -6 nT (aurora-friendly when strongly negative).
    if (!bz.isNullOrEmpty()) {
        val range = computeNiceRange(bz, preferredMin = -20.0, preferredMax = 20.0)
        out += GraphSeries(
            title = "Bz",
            unit = "нТл",
            points = points(bz, ""),
            minY = range.first,
            maxY = range.second,
            gridStepY = 2.0,
            dangerBelow = -6.0,
            dangerAbove = null
        )
    }

    // Speed (step 100). Danger above ~750 km/s
    if (!speed.isNullOrEmpty()) {
        val range = computeNiceRange(speed, preferredMin = 200.0, preferredMax = 1200.0)
        out += GraphSeries(
            title = "Speed",
            unit = "км/с",
            points = points(speed, ""),
            minY = range.first,
            maxY = range.second,
            gridStepY = 100.0,
            dangerBelow = null,
            dangerAbove = 750.0
        )
    }

    // Density (step 10). Danger above ~25
    if (!density.isNullOrEmpty()) {
        val range = computeNiceRange(density, preferredMin = 0.0, preferredMax = 50.0)
        out += GraphSeries(
            title = "Плотность",
            unit = "",
            points = points(density, ""),
            minY = range.first,
            maxY = range.second,
            gridStepY = 10.0,
            dangerBelow = null,
            dangerAbove = 25.0
        )
    }

    // Optional: Bx series if exists (step 5)
    if (!bx.isNullOrEmpty()) {
        val range = computeNiceRange(bx, preferredMin = -30.0, preferredMax = 30.0)
        out += GraphSeries(
            title = "Bx",
            unit = "нТл",
            points = points(bx, ""),
            minY = range.first,
            maxY = range.second,
            gridStepY = 5.0,
            dangerBelow = null,
            dangerAbove = null
        )
    }

    return out
}

/* ------------------------- helpers ------------------------- */

private fun computeNiceRange(values: List<Double>, preferredMin: Double, preferredMax: Double): Pair<Double, Double> {
    val vMin = values.minOrNull() ?: preferredMin
    val vMax = values.maxOrNull() ?: preferredMax
    val rawMin = min(vMin, preferredMin)
    val rawMax = max(vMax, preferredMax)
    // pad by 5%
    val pad = (rawMax - rawMin).let { if (it <= 0.0) 1.0 else it } * 0.05
    val lo = floor((rawMin - pad))
    val hi = ceil((rawMax + pad))
    return lo to hi
}

/**
 * Try to read a numeric series from state.
 * Supports:
 *  - List<Number>
 *  - List<Pair<*, Number>> (takes second)
 *  - List<Any> where element has fields/getters like "value"
 */
private fun readDoubleSeries(state: Any, names: List<String>): List<Double>? {
    val raw = names.asSequence()
        .mapNotNull { getProperty(state, it) }
        .firstOrNull()

    val list = raw as? List<*> ?: return null
    val out = mutableListOf<Double>()

    for (e in list) {
        when (e) {
            null -> continue
            is Number -> out += e.toDouble()
            else -> {
                // Pair-like (component2)
                val c2 = runCatching { e.javaClass.getMethod("component2").invoke(e) }.getOrNull()
                if (c2 is Number) {
                    out += c2.toDouble()
                    continue
                }
                // object.value
                val v = getProperty(e, "value")
                if (v is Number) {
                    out += v.toDouble()
                    continue
                }
                // fallback: try "v"
                val v2 = getProperty(e, "v")
                if (v2 is Number) {
                    out += v2.toDouble()
                    continue
                }
            }
        }
    }

    return out.takeIf { it.isNotEmpty() }
}

/**
 * Try to read time labels from state:
 *  - List<String>
 *  - List<Instant>
 *  - List<Long> epoch millis
 *  - List<Pair<Instant, *>> (takes first)
 */
private fun readTimeLabels(state: Any, names: List<String>): List<String>? {
    val raw = names.asSequence()
        .mapNotNull { getProperty(state, it) }
        .firstOrNull() ?: return null

    val list = raw as? List<*> ?: return null
    val fmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
    val out = mutableListOf<String>()

    for (e in list) {
        when (e) {
            null -> continue
            is String -> out += e
            is Instant -> out += fmt.format(e)
            is Number -> {
                // assume epoch millis
                val inst = Instant.ofEpochMilli(e.toLong())
                out += fmt.format(inst)
            }
            else -> {
                // Pair-like component1
                val c1 = runCatching { e.javaClass.getMethod("component1").invoke(e) }.getOrNull()
                when (c1) {
                    is Instant -> out += fmt.format(c1)
                    is Number -> out += fmt.format(Instant.ofEpochMilli(c1.toLong()))
                    is String -> out += c1
                }
            }
        }
    }

    return out.takeIf { it.isNotEmpty() }
}

private fun getProperty(obj: Any, name: String): Any? {
    val cls = obj.javaClass

    // 1) Kotlin/Java getter: getXxx()
    val getter = "get" + name.replaceFirstChar { it.uppercaseChar() }
    runCatching { cls.getMethod(getter).invoke(obj) }.getOrNull()?.let { return it }

    // 2) boolean-ish: isXxx()
    val isGetter = "is" + name.replaceFirstChar { it.uppercaseChar() }
    runCatching { cls.getMethod(isGetter).invoke(obj) }.getOrNull()?.let { return it }

    // 3) public field
    runCatching { cls.getField(name).get(obj) }.getOrNull()?.let { return it }

    // 4) declared field (private)
    runCatching {
        cls.getDeclaredField(name).apply { isAccessible = true }.get(obj)
    }.getOrNull()?.let { return it }

    return null
}