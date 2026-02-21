package com.example.wittyapp.domain

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Robust helpers to handle APIs that sometimes return:
 *  - JsonArray
 *  - JsonObject containing array under "data"/"values"/"result"/etc.
 */
object JsonSafe {

    fun asArrayOrNull(root: JsonElement?): JsonArray? {
        if (root == null || root is JsonNull) return null
        return when (root) {
            is JsonArray -> root
            is JsonObject -> {
                // common wrappers
                val keys = listOf(
                    "data", "values", "result", "records", "items", "list",
                    "Data", "Values", "Result"
                )
                for (k in keys) {
                    val v = root[k]
                    if (v is JsonArray) return v
                }
                // sometimes nested like { "data": { "values": [...] } }
                for (k in keys) {
                    val v = root[k]
                    if (v is JsonObject) {
                        for (k2 in keys) {
                            val v2 = v[k2]
                            if (v2 is JsonArray) return v2
                        }
                    }
                }
                null
            }
            else -> null
        }
    }

    fun obj(root: JsonElement?): JsonObject? =
        runCatching { root?.jsonObject }.getOrNull()

    fun str(obj: JsonObject?, key: String): String? =
        runCatching { obj?.get(key)?.jsonPrimitive?.content }.getOrNull()

    fun dbl(obj: JsonObject?, key: String): Double? =
        runCatching { obj?.get(key)?.jsonPrimitive?.content?.toDoubleOrNull() }.getOrNull()

    fun lng(obj: JsonObject?, key: String): Long? =
        runCatching { obj?.get(key)?.jsonPrimitive?.content?.toLongOrNull() }.getOrNull()
}