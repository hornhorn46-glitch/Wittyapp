package com.example.wittyapp

sealed class Screen {

    object EARTH_HOME : Screen()
    object SUN_HOME : Screen()

    object EARTH_GRAPHS : Screen()
    object EARTH_EVENTS : Screen()

    object SETTINGS : Screen()
    object TUTORIAL : Screen()

    data class FULL(val url: String, val title: String) : Screen()

    companion object {
        fun rootFor(mode: AppMode): Screen =
            if (mode == AppMode.EARTH) EARTH_HOME else SUN_HOME
    }
}