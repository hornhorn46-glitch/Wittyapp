package com.example.wittyapp.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

private val json = Json { ignoreUnknownKeys = true }

fun parseKp1m(body: String): List<KpSample> {
    val el = json.parseToJsonElement(body)
    val arr = el.jsonArray
    return arr.mapNotNull { it.jsonObject.let { o ->
        val time = o["time_tag"]?.jsonPrimitive?.contentOrNull ?: return@let null
        val kp = o["kp_index"]?.jsonPrimitive?.doubleOrNull ?: return@let null
        KpSample(Instant.parse(time), kp)
    } }
}

fun parseWind1m(body: String): List<WindSample> {
    val el = json.parseToJsonElement(body)
    val arr = el.jsonArray
    return arr.mapNotNull { it.jsonObject.let { o ->
        val time = o["time_tag"]?.jsonPrimitive?.contentOrNull ?: return@let null
        val speed = o["speed"]?.jsonPrimitive?.doubleOrNull ?: return@let null
        val density = o["density"]?.jsonPrimitive?.doubleOrNull
        WindSample(Instant.parse(time), speed, density)
    } }
}

fun parseMag1m(body: String): List<MagSample> {
    val el = json.parseToJsonElement(body)
    val arr = el.jsonArray
    return arr.mapNotNull { it.jsonObject.let { o ->
        val time = o["time_tag"]?.jsonPrimitive?.contentOrNull ?: return@let null
        val bx = o["bx_gsm"]?.jsonPrimitive?.doubleOrNull ?: o["bx_gse"]?.jsonPrimitive?.doubleOrNull
        val by = o["by_gsm"]?.jsonPrimitive?.doubleOrNull ?: o["by_gse"]?.jsonPrimitive?.doubleOrNull
        val bz = o["bz_gsm"]?.jsonPrimitive?.doubleOrNull ?: o["bz_gse"]?.jsonPrimitive?.doubleOrNull
        MagSample(Instant.parse(time), bx, by, bz)
    } }
}
