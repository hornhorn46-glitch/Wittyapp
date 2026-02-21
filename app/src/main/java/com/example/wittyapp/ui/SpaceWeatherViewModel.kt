package com.example.wittyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wittyapp.domain.*
import com.example.wittyapp.net.SpaceWeatherApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class SpaceWeatherUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val updatedAt: Instant? = null,

    val kpNow: Double? = null,
    val kp3hAvg: Double? = null,

    val speedNow: Double? = null,
    val densityNow: Double? = null,
    val bzNow: Double? = null,

    val speed3hAvg: Double? = null,
    val density3hAvg: Double? = null,
    val bz3hAvg: Double? = null,

    // v3
    val bzMin3h: Double? = null,
    val fracBzNegative3h: Double? = null,

    val auroraScore: Int = 0,
    val auroraTitle: String = "—",
    val auroraDetails: String = "",

    val events: List<DonkiEvent> = emptyList(),

    // v3 графики (24ч)
    val kp24: List<GraphPoint> = emptyList(),
    val bz24: List<GraphPoint> = emptyList(),
    val speed24: List<GraphPoint> = emptyList()
)

data class DonkiEvent(
    val type: String,
    val title: String,
    val timeTag: String,
    val note: String? = null
)

class SpaceWeatherViewModel(
    private val api: SpaceWeatherApi
) : ViewModel() {

    var state: SpaceWeatherUiState = SpaceWeatherUiState(loading = true)
        private set

    fun refresh() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)

            try {
                val kpDef = async { api.fetchKp() }
                val plasmaDef = async { api.fetchSolarWindPlasma1d() }
                val magDef = async { api.fetchSolarWindMag1d() }

                val kpRaw = kpDef.await()
                val plasmaRaw = plasmaDef.await()
                val magRaw = magDef.await()

                val now = Instant.now()
                val from3h = now.minusSeconds(3 * 3600)

                val kpSeries = parseKpSeries(kpRaw)
                val plasmaSeries = parsePlasmaSeries(plasmaRaw)
                val magSeries = parseMagSeries(magRaw)

                val kpNow = kpSeries.lastOrNull()?.kp
                val kp3hAvg = kpSeries
                    .filter { it.time >= from3h }
                    .map { it.kp as Double? }
                    .avgOrNull()

                val plasmaNow = plasmaSeries.lastOrNull()
                val magNow = magSeries.lastOrNull()

                val speedNow = plasmaNow?.speedKmS
                val densityNow = plasmaNow?.densityCC
                val bzNow = magNow?.bzNt

                val speed3hAvg = plasmaSeries.filter { it.time >= from3h }.map { it.speedKmS }.avgOrNull()
                val density3hAvg = plasmaSeries.filter { it.time >= from3h }.map { it.densityCC }.avgOrNull()
                val bz3hAvg = magSeries.filter { it.time >= from3h }.map { it.bzNt }.avgOrNull()

                // v3: Bz минимум и доля Bz<0
                val bzValues3h = magSeries
                    .filter { it.time >= from3h }
                    .mapNotNull { it.bzNt }

                val bzMin3h = bzValues3h.minOrNull()

                val fracBzNegative3h = run {
                    val total = bzValues3h.size
                    if (total == 0) null
                    else bzValues3h.count { it < 0 }.toDouble() / total.toDouble()
                }

                val prediction = predictAuroraV3(
                    kpNow = kpNow,
                    kp3hAvg = kp3hAvg,
                    bzNow = bzNow,
                    bz3hAvg = bz3hAvg,
                    bzMin3h = bzMin3h,
                    fracBzNegative3h = fracBzNegative3h,
                    speedNow = speedNow,
                    speed3hAvg = speed3hAvg,
                    densityNow = densityNow,
                    density3hAvg = density3hAvg
                )

                val events = fetchDonkiEvents(daysBack = 5)

                // v3 графики (24 часа)
                val kp24 = kpToGraph24h(kpSeries, now)
                val bz24 = bzToGraph24h(magSeries, now)
                val speed24 = speedToGraph24h(plasmaSeries, now)

                state = SpaceWeatherUiState(
                    loading = false,
                    error = null,
                    updatedAt = now,

                    kpNow = kpNow,
                    kp3hAvg = kp3hAvg,

                    speedNow = speedNow,
                    densityNow = densityNow,
                    bzNow = bzNow,

                    speed3hAvg = speed3hAvg,
                    density3hAvg = density3hAvg,
                    bz3hAvg = bz3hAvg,

                    bzMin3h = bzMin3h,
                    fracBzNegative3h = fracBzNegative3h,

                    auroraScore = prediction.score,
                    auroraTitle = prediction.title,
                    auroraDetails = prediction.details,

                    events = events,

                    kp24 = kp24,
                    bz24 = bz24,
                    speed24 = speed24
                )

            } catch (t: Throwable) {
                state = state.copy(
                    loading = false,
                    error = t.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun startAutoRefresh(periodMs: Long = 5 * 60 * 1000L) {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(periodMs)
            }
        }
    }

    private suspend fun fetchDonkiEvents(daysBack: Long): List<DonkiEvent> {
        val zone = ZoneId.systemDefault()
        val end = LocalDate.now(zone)
        val start = end.minusDays(daysBack)
        val startStr = start.toString()
        val endStr = end.toString()

        val cme = runCatching { api.fetchDonkiCme(startStr, endStr) }.getOrDefault(JsonArray(emptyList()))
        val flr = runCatching { api.fetchDonkiFlares(startStr, endStr) }.getOrDefault(JsonArray(emptyList()))
        val gst = runCatching { api.fetchDonkiGeomagneticStorm(startStr, endStr) }.getOrDefault(JsonArray(emptyList()))

        return buildList {
            addAll(cme.mapNotNull { it.toDonkiEvent("CME") })
            addAll(flr.mapNotNull { it.toDonkiEvent("FLR") })
            addAll(gst.mapNotNull { it.toDonkiEvent("GST") })
        }.sortedByDescending { it.timeTag }
    }
}

private fun JsonElement.toDonkiEvent(type: String): DonkiEvent? {
    val o = this as? JsonObject ?: return null

    val time =
        o["startTime"]?.jsonPrimitive?.contentOrNull
            ?: o["peakTime"]?.jsonPrimitive?.contentOrNull
            ?: o["time21_30"]?.jsonPrimitive?.contentOrNull
            ?: o["submissionTime"]?.jsonPrimitive?.contentOrNull
            ?: return null

    val title =
        o["activityID"]?.jsonPrimitive?.contentOrNull
            ?: o["flrID"]?.jsonPrimitive?.contentOrNull
            ?: o["gstID"]?.jsonPrimitive?.contentOrNull
            ?: "Событие"

    val note =
        o["note"]?.jsonPrimitive?.contentOrNull
            ?: o["sourceLocation"]?.jsonPrimitive?.contentOrNull

    return DonkiEvent(type = type, title = title, timeTag = time, note = note)
}

private fun Iterable<Double?>.avgOrNull(): Double? {
    val v = this.filterNotNull()
    return if (v.isEmpty()) null else v.average()
}