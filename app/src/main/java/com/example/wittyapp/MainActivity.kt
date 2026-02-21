package com.example.wittyapp

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.*
import com.example.wittyapp.ui.settings.SettingsStore
import com.example.wittyapp.ui.strings.AppStrings
import com.example.wittyapp.ui.theme.CosmosTheme
import com.example.wittyapp.ui.topbar.ModeToggleRuneButton
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = SpaceWeatherApi()

        setContent {
            val vm: SpaceWeatherViewModel =
                viewModel(factory = SimpleFactory { SpaceWeatherViewModel(api) })

            val store = remember { SettingsStore(this) }
            var lang by remember { mutableStateOf(store.getLanguage()) }
            val strings = remember(lang) { AppStrings(lang) }

            var mode by remember { mutableStateOf(store.getMode()) }
            var stack by remember { mutableStateOf(listOf(Screen.rootFor(mode))) }

            val current = stack.last()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            fun push(s: Screen) { stack = stack + s }
            fun pop(): Boolean =
                if (stack.size > 1) { stack = stack.dropLast(1); true } else false
            fun setRoot(s: Screen) { stack = listOf(s) }

            var lastBackAt by remember { mutableStateOf(0L) }

            BackHandler {
                if (pop()) return@BackHandler
                val now = SystemClock.elapsedRealtime()
                if (now - lastBackAt < 1800L) finish()
                else {
                    lastBackAt = now
                    scope.launch {
                        snackbarHostState.showSnackbar(strings.exitHint)
                    }
                }
            }

            CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    if (mode == AppMode.EARTH)
                                        strings.titleEarth
                                    else
                                        strings.titleSun
                                )
                            },
                            actions = {
                                IconButton(onClick = { push(Screen.TUTORIAL) }) {
                                    Icon(Icons.Default.MenuBook, null)
                                }

                                ModeToggleRuneButton(
                                    mode = mode,
                                    onToggle = {
                                        mode = if (mode == AppMode.EARTH)
                                            AppMode.SUN else AppMode.EARTH
                                        store.setMode(mode)
                                        setRoot(Screen.rootFor(mode))
                                    }
                                )

                                IconButton(onClick = { push(Screen.SETTINGS) }) {
                                    Icon(Icons.Default.Settings, null)
                                }
                            }
                        )
                    }
                ) { pad ->

                    AnimatedContent(
                        targetState = current,
                        transitionSpec = {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(180))
                        },
                        label = "nav"
                    ) { s ->

                        when (s) {
                            Screen.EARTH_HOME -> NowScreen(
                                vm = vm,
                                mode = AppMode.EARTH,
                                strings = strings,
                                contentPadding = pad,
                                onOpenGraphs = { push(Screen.EARTH_GRAPHS) },
                                onOpenEvents = { push(Screen.EARTH_EVENTS) }
                            )

                            Screen.SUN_HOME -> SunScreen(
                                strings = strings,
                                contentPadding = pad,
                                onOpenFull = { url, title ->
                                    push(Screen.FULL(url, title))
                                }
                            )

                            Screen.SETTINGS ->
                                SettingsScreen(strings, pad, lang, {
                                    lang = it
                                    store.setLanguage(it)
                                }) { pop() }

                            Screen.TUTORIAL ->
                                TutorialScreen(strings, pad) { pop() }

                            is Screen.FULL ->
                                FullscreenWebImageScreen(
                                    s.url,
                                    s.title,
                                    strings,
                                    pad
                                ) { pop() }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Simple ViewModel Factory ---------- */

class SimpleFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {

    override fun <T2 : ViewModel> create(modelClass: Class<T2>): T2 {
        @Suppress("UNCHECKED_CAST")
        return creator() as T2
    }
}