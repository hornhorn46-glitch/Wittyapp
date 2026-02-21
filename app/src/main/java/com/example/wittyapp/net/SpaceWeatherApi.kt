package com.example.wittyapp.net

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

class SpaceWeatherApi(
    private val nasaApiKey: String = "DEMO_KEY"
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
    }

    // NOAA SWPC
    suspend fun fetchKp(): JsonElement =
        client.get("https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json").body()

    suspend fun fetchSolarWindPlasma1d(): JsonElement =
        client.get("https://services.swpc.noaa.gov/products/solar-wind/plasma-1-day.json").body()

    suspend fun fetchSolarWindMag1d(): JsonElement =
        client.get("https://services.swpc.noaa.gov/products/solar-wind/mag-1-day.json").body()

    // NASA DONKI (через api.nasa.gov)
    suspend fun fetchDonkiCme(startDate: String, endDate: String): JsonArray =
        client.get("https://api.nasa.gov/DONKI/CME") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            parameter("api_key", nasaApiKey)
        }.body()

    suspend fun fetchDonkiFlares(startDate: String, endDate: String): JsonArray =
        client.get("https://api.nasa.gov/DONKI/FLR") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            parameter("api_key", nasaApiKey)
        }.body()

    suspend fun fetchDonkiGeomagneticStorm(startDate: String, endDate: String): JsonArray =
        client.get("https://api.nasa.gov/DONKI/GST") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            parameter("api_key", nasaApiKey)
        }.body()
}