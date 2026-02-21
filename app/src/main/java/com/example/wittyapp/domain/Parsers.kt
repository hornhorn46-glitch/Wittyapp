package com.example.wittyapp.domain

import kotlinx.serialization.json.*
import java.time.Instant

private val json = Json { ignoreUnknownKeys = true }

fun parseKp1m(body: String): List<KpSample> {
    val arr = json.parseToJsonElement(body).jsonArray
    return arr.mapNotNull { el ->
        val obj = el.jsonObject
        val time = obj["time_tag"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val kp = obj["kp_index"]?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null

        runCatching {
            KpSample(
                t = Instant.parse(time),
                kp = kp
            )
        }.getOrNull()
    }
}

fun parseWind1m(body: String): List<WindSample> {
    val arr = json.parseToJsonElement(body).jsonArray
    return arr.mapNotNull { el ->
        val obj = el.jsonObject
        val time = obj["time_tag"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val speed = obj["proton_speed"]?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null
        val density = obj["proton_density"]?.jsonPrimitive?.doubleOrNull

        runCatching {
            WindSample(
                t = Instant.parse(time),
                speed = speed,
                density = density
            )
        }.getOrNull()
    }
}

fun parseMag1m(body: String): List<MagSample> {
    val arr = json.parseToJsonElement(body).jsonArray
    return arr.mapNotNull { el ->
        val obj = el.jsonObject
        val time = obj["time_tag"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val bx = obj["bx_gsm"]?.jsonPrimitive?.doubleOrNull
        val by = obj["by_gsm"]?.jsonPrimitive?.doubleOrNull
        val bz = obj["bz_gsm"]?.jsonPrimitive?.doubleOrNull

        runCatching {
            MagSample(
                t = Instant.parse(time),
                bx = bx,
                by = by,
                bz = bz
            )
        }.getOrNull()
    }
}