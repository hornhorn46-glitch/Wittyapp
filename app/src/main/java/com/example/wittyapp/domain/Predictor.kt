package com.example.wittyapp.domain

data class AuroraPrediction(
    val score: Int,      // 0..100
    val title: String,
    val details: String
)

/**
 * v3: точнее за счёт:
 * - fracBzNegative3h: доля времени (0..1), когда Bz < 0 за 3 часа
 * - bzMin3h: минимум Bz за 3 часа (самый отрицательный)
 */
fun predictAuroraV3(
    kpNow: Double?,
    kp3hAvg: Double?,
    bzNow: Double?,
    bz3hAvg: Double?,
    bzMin3h: Double?,
    fracBzNegative3h: Double?,
    speedNow: Double?,
    speed3hAvg: Double?,
    densityNow: Double?,
    density3hAvg: Double?
): AuroraPrediction {

    val kpAvg = kp3hAvg ?: (kpNow ?: 0.0)
    val kpCur = kpNow ?: kpAvg

    val bzCur = bzNow ?: bz3hAvg
    val bzAvg = bz3hAvg
    val bzMin = bzMin3h

    val speedAvg = speed3hAvg ?: speedNow
    val speedCur = speedNow ?: speedAvg

    val densAvg = density3hAvg ?: densityNow
    val densCur = densityNow ?: densAvg

    var score = 0.0

    // Kp — базовая активность
    score += when {
        kpAvg < 2.5 -> 5.0
        kpAvg < 4.0 -> 22.0
        kpAvg < 5.0 -> 38.0
        kpAvg < 6.0 -> 58.0
        kpAvg < 7.0 -> 74.0
        kpAvg < 8.0 -> 87.0
        else -> 96.0
    }

    // Bz: текущий знак/уровень
    if (bzCur != null) {
        score += when {
            bzCur <= -10 -> 24.0
            bzCur <= -6 -> 17.0
            bzCur <= -3 -> 9.0
            bzCur < 0 -> 5.0
            else -> -8.0
        }
    }

    // Bz: среднее (устойчивость)
    if (bzAvg != null) {
        score += when {
            bzAvg <= -6 -> 9.0
            bzAvg <= -3 -> 5.0
            bzAvg < 0 -> 2.0
            else -> -4.0
        }
    }

    // Bz: минимум (пики “минуса”)
    if (bzMin != null) {
        score += when {
            bzMin <= -15 -> 8.0
            bzMin <= -10 -> 6.0
            bzMin <= -6 -> 4.0
            else -> 0.0
        }
    }

    // Доля времени с Bz<0 за 3ч (самое ценное улучшение)
    if (fracBzNegative3h != null) {
        score += when {
            fracBzNegative3h >= 0.85 -> 10.0
            fracBzNegative3h >= 0.65 -> 7.0
            fracBzNegative3h >= 0.45 -> 4.0
            fracBzNegative3h >= 0.25 -> 2.0
            else -> -2.0
        }
    }

    // Скорость
    if (speedAvg != null) {
        score += when {
            speedAvg >= 750 -> 14.0
            speedAvg >= 650 -> 9.0
            speedAvg >= 550 -> 5.0
            speedAvg >= 450 -> 2.0
            else -> 0.0
        }
    }

    // Плотность
    if (densAvg != null) {
        score += when {
            densAvg >= 20 -> 6.0
            densAvg >= 12 -> 4.0
            densAvg >= 7 -> 2.0
            else -> 0.0
        }
    }

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
        append("За 3 часа: ")
        append("Kp≈${kpAvg.f1()}, ")
        append("Bz≈${bzAvg?.f1() ?: "—"} нТ, ")
        append("min Bz=${bzMin?.f1() ?: "—"} нТ, ")
        append("Bz<0: ${(fracBzNegative3h?.times(100))?.f0() ?: "—"}%, ")
        append("V≈${speedAvg?.f0() ?: "—"} км/с, ")
        append("n≈${densAvg?.f1() ?: "—"}.\n")

        append("Сейчас: ")
        append("Kp=${kpCur.f1()}, ")
        append("Bz=${bzCur?.f1() ?: "—"} нТ, ")
        append("V=${speedCur?.f0() ?: "—"} км/с, ")
        append("n=${densCur?.f1() ?: "—"}.\n")

        append("Оценка: $s/100.")
    }

    return AuroraPrediction(score = s, title = title, details = details)
}

private fun Double.f0(): String = String.format("%.0f", this)
private fun Double.f1(): String = String.format("%.1f", this)