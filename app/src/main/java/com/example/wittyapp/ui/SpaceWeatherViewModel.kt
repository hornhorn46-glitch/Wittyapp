package com.example.wittyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wittyapp.domain.*
import com.example.wittyapp.net.SpaceWeatherApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class SpaceWeatherUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val updatedAt: Instant? = null,

    val kpNow: Double? = null,
    val speedNow: Double? = null,
    val densityNow: Double? = null,
    val bxNow: Double? = null,
    val bzNow: Double? = null,

    val auroraScore: Int = 0,
    val auroraTitle: String = "",
    val auroraDetails: String = "",

    val kpSeries24h: List<GraphPoint> = emptyList(),
    val speedSeries24h: List<GraphPoint> = emptyList(),
    val bzSeries24h: List<GraphPoint> = emptyList(),
)

class SpaceWeatherViewModel(
    private val api: SpaceWeatherApi
) : ViewModel() {

    var state: SpaceWeatherUiState = SpaceWeatherUiState()
        private set

    private var autoJob: Job? = null

    fun startAutoRefresh(periodMs: Long) {
        autoJob?.cancel()
        autoJob = viewModelScope.launch {
            while (true) {
                delay(periodMs)
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                state = state.copy(loading = true, error = null)

                val kpBody = api.kp1mJson()
                val windBody = api.wind1mJson()
                val magBody = api.mag1mJson()

                // Быстрые проверки "это вообще JSON?"
                val maybeHtml =
                    kpBody.trimStart().startsWith("<") ||
                    windBody.trimStart().startsWith("<") ||
                    magBody.trimStart().startsWith("<")

                if (maybeHtml) {
                    state = state.copy(
                        loading = false,
                        updatedAt = Instant.now(),
                        error = buildString {
                            appendLine("Похоже, сервер вернул HTML, а не JSON.")
                            appendLine("kp head: ${kpBody.head()}")
                            appendLine("wind head: ${windBody.head()}")
                            appendLine("mag head: ${magBody.head()}")
                        }
                    )
                    return@launch
                }

                val kp = runCatching { parseKp1m(kpBody) }.getOrElse { emptyList() }
                val wind = runCatching { parseWind1m(windBody) }.getOrElse { emptyList() }
                val mag = runCatching { parseMag1m(magBody) }.getOrElse { emptyList() }

                // Если парсинг вернул пусто — покажем диагностический кусок ответа
                if (kp.isEmpty() && wind.isEmpty() && mag.isEmpty()) {
                    state = state.copy(
                        loading = false,
                        updatedAt = Instant.now(),
                        error = buildString {
                            appendLine("Данные не распознаны (парсер вернул пусто).")
                            appendLine("Нужно подогнать парсеры под реальный формат API.")
                            appendLine()
                            appendLine("kp head: ${kpBody.head()}")
                            appendLine("wind head: ${windBody.head()}")
                            appendLine("mag head: ${magBody.head()}")
                        }
                    )
                    return@launch
                }

                val now = Instant.now()

                val kpNow = kp.lastOrNull()?.kp
                val windNow = wind.lastOrNull()
                val magNow = mag.lastOrNull()

                val since3h = now.minus(3, ChronoUnit.HOURS)
                val kp3h = kp.filter { it.t.isAfter(since3h) }.map { it.kp }.averageOrNull()
                val sp3h = wind.filter { it.t.isAfter(since3h) }.map { it.speed }.averageOrNull()
                val bz3h = mag.filter { it.t.isAfter(since3h) }.mapNotNull { it.bz }.averageOrNull()

                val pred = predictAurora3h(kp3h, sp3h, bz3h)

                val since24h = now.minus(24, ChronoUnit.HOURS)
                val kp24 = kp.filter { it.t.isAfter(since24h) }.map { GraphPoint(it.t, it.kp) }
                val sp24 = wind.filter { it.t.isAfter(since24h) }.map { GraphPoint(it.t, it.speed) }
                val bz24 = mag.filter { it.t.isAfter(since24h) }
                    .mapNotNull { s -> s.bz?.let { GraphPoint(s.t, it) } }

                // Если частично пусто — тоже покажем мягкое предупреждение в error (не блокируем UI)
                val warn = buildString {
                    if (kp.isEmpty()) appendLine("⚠ kp пусто (парсер/источник). kp head: ${kpBody.head()}")
                    if (wind.isEmpty()) appendLine("⚠ wind пусто (парсер/источник). wind head: ${windBody.head()}")
                    if (mag.isEmpty()) appendLine("⚠ mag пусто (парсер/источник). mag head: ${magBody.head()}")
                }.trim().ifBlank { null }

                state = state.copy(
                    loading = false,
                    updatedAt = now,
                    error = warn,
                    kpNow = kpNow,
                    speedNow = windNow?.speed,
                    densityNow = windNow?.density,
                    bxNow = magNow?.bx,
                    bzNow = magNow?.bz,
                    auroraScore = pred.score,
                    auroraTitle = pred.title,
                    auroraDetails = pred.details,
                    kpSeries24h = kp24,
                    speedSeries24h = sp24,
                    bzSeries24h = bz24
                )
            } catch (e: Exception) {
                state = state.copy(loading = false, error = e.message ?: e.toString())
            }
        }
    }

    fun simpleGraphSeries(): GraphSeries = GraphSeries(
        kp = state.kpSeries24h,
        bz = state.bzSeries24h,
        speed = state.speedSeries24h
    )
}

data class GraphSeries(
    val kp: List<GraphPoint>,
    val bz: List<GraphPoint>,
    val speed: List<GraphPoint>
)

private fun List<Double>.averageOrNull(): Double? = if (isEmpty()) null else average()

private fun String.head(n: Int = 320): String =
    this.replace("\n", " ")
        .replace("\r", " ")
        .trim()
        .take(n)