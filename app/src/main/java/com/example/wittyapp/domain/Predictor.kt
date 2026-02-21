package com.example.wittyapp.domain

data class AuroraPrediction(
    val score: Int,      // 0..100
    val title: String,
    val details: String
)

/**
 * Эвристика (v2):
 * - Kp (сейчас + среднее за 3 часа)
 * - Bz (сейчас + среднее за 3 часа): отрицательный Bz сильно повышает шанс
 * - скорость/плотность солнечного ветра
 */
fun predictAurora(
    kpNow: Double?,
    kp3hAvg: Double?,
    bzNow: Double?,
    bz3hAvg: Double?,
    speedNow: Double?,
    speed3hAvg: Double?,
    densityNow: Double?,
    density3hAvg: Double?
): AuroraPrediction {

    val kp = kpNow ?: kp3hAvg ?: 0.0
    val kpAvg = kp3hAvg ?: kp

    val bz = bzNow ?: bz3hAvg
    val bzAvg = bz3hAvg

    val speed = speedNow ?: speed3hAvg
    val speedAvg = speed3hAvg

    val dens = densityNow ?: density3hAvg
    val densAvg = density3hAvg

    var score = 0.0

    // Kp — базовая сила геомагнитной активности
    score += when {
        kpAvg < 2.5 -> 5.0
        kpAvg < 4.0 -> 20.0
        kpAvg < 5.0 -> 35.0
        kpAvg < 6.0 -> 55.0
        kpAvg < 7.0 -> 72.0
        kpAvg < 8.0 -> 85.0
        else -> 95.0
    }

    // Bz — ключ к “впуску” энергии в магнитосферу
    if (bz != null) {
        score += when {
            bz <= -10 -> 25.0
            bz <= -6 -> 18.0
            bz <= -3 -> 10.0
            bz < 0 -> 6.0
            else -> -8.0
        }
    }

    if (bzAvg != null) {
        score += when {
            bzAvg <= -6 -> 10.0
            bzAvg <= -3 -> 6.0
            bzAvg < 0 -> 3.0
            else -> -4.0
        }
    }

    // Скорость
    if (speedAvg != null) {
        score += when {
            speedAvg >= 750 -> 15.0
            speedAvg >= 650 -> 10.0
            speedAvg >= 550 -> 6.0
            speedAvg >= 450 -> 2.0
            else -> 0.0
        }
    }

    // Плотность — слабее влияет, но помогает
    if (densAvg != null) {
        score += when {
            densAvg >= 20 -> 6.0
            densAvg >= 12 -> 4.0
            densAvg >= 7 -> 2.0
            else -> 0.0
        }
    }

    // Нормализация
    score = score.coerceIn(0.0, 100.0)
    val s = score.toInt()

    val title = when {
        s < 20 -> "Шанс низкий"
        s < 45 -> "Шанс небольшой"
        s < 70 -> "Шанс средний"
        s < 85 -> "Шанс высокий"
        else -> "Очень высокий шанс"
    }

    val details = buildString {
        append("За последние 3 часа: ")
        append("Kp≈${kpAvg.format1()}, ")
        append("Bz≈${bzAvg?.format1() ?: "—"} нТ, ")
        append("V≈${speedAvg?.format0() ?: "—"} км/с, ")
        append("n≈${densAvg?.format1() ?: "—"}.\n")

        append("Сейчас: ")
        append("Kp=${kp.format1()}, ")
        append("Bz=${bz?.format1() ?: "—"} нТ, ")
        append("V=${speed?.format0() ?: "—"} км/с, ")
        append("n=${dens?.format1() ?: "—"}.\n")

        append("Оценка: $s/100.")
    }

    return AuroraPrediction(score = s, title = title, details = details)
}

private fun Double.format0(): String = String.format("%.0f", this)
private fun Double.format1(): String = String.format("%.1f", this)