package com.example.wittyapp.domain

import kotlinx.serialization.json.*
import java.time.Instant

fun parseKp1m(json: String): List<KpSample> {
    val arr = Json.parseToJsonElement(json).jsonArray
    return arr.drop(1).mapNotNull {
        val row = it.jsonArray
        val t = row[0].jsonPrimitive.contentOrNull ?: return@mapNotNull null
        val kp = row[1].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
        KpSample(Instant.parse(t), kp)
    }
}

fun parseWind1m(json: String): List<WindSample> {
    val arr = Json.parseToJsonElement(json).jsonArray
    return arr.drop(1).mapNotNull {
        val row = it.jsonArray
        val t = row[0].jsonPrimitive.contentOrNull ?: return@mapNotNull null
        val speed = row[1].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
        val density = row[2].jsonPrimitive.doubleOrNull
        WindSample(Instant.parse(t), speed, density)
    }
}

fun parseMag1m(json: String): List<MagSample> {
    val arr = Json.parseToJsonElement(json).jsonArray
    return arr.drop(1).mapNotNull {
        val row = it.jsonArray
        val t = row[0].jsonPrimitive.contentOrNull ?: return@mapNotNull null
        val bx = row[1].jsonPrimitive.doubleOrNull
        val by = row[2].jsonPrimitive.doubleOrNull
        val bz = row[3].jsonPrimitive.doubleOrNull
        MagSample(Instant.parse(t), bx, by, bz)
    }
}