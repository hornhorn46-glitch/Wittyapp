package com.example.wittyapp

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "witty_clicker")

data class GameState(
    val coins: Long = 0,
    val power: Int = 1,          // монет за клик
    val autoPerSec: Int = 0      // монет в секунду
)

class GameStore(private val context: Context) {

    private object Keys {
        val COINS = longPreferencesKey("coins")
        val POWER = intPreferencesKey("power")
        val AUTO = intPreferencesKey("auto_per_sec")
    }

    val state: Flow<GameState> = context.dataStore.data.map { prefs ->
        GameState(
            coins = prefs[Keys.COINS] ?: 0L,
            power = prefs[Keys.POWER] ?: 1,
            autoPerSec = prefs[Keys.AUTO] ?: 0
        )
    }

    suspend fun addCoins(delta: Long) {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.COINS] ?: 0L
            prefs[Keys.COINS] = (cur + delta).coerceAtLeast(0)
        }
    }

    suspend fun spendCoins(cost: Long): Boolean {
        var ok = false
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.COINS] ?: 0L
            if (cur >= cost) {
                prefs[Keys.COINS] = cur - cost
                ok = true
            }
        }
        return ok
    }

    suspend fun upgradePower() {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.POWER] ?: 1
            prefs[Keys.POWER] = (cur + 1).coerceAtMost(999)
        }
    }

    suspend fun upgradeAuto() {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.AUTO] ?: 0
            prefs[Keys.AUTO] = (cur + 1).coerceAtMost(999)
        }
    }

    suspend fun reset() {
        context.dataStore.edit { it.clear() }
    }
}