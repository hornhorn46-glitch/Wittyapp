package com.example.wittyapp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.*
import com.example.wittyapp.ui.settings.SettingsStore
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.AppStrings
import com.example.wittyapp.ui.theme.CosmosTheme
import com.example.wittyapp.ui.topbar.ModeToggleRuneButton

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
            fun push(s: Screen) { stack = stack + s }
            fun pop(): Boolean = if (stack.size > 1) { stack = stack.dropLast(1); true } else false
            fun setRoot(s: Screen) { stack = listOf(s) }
            val current = stack.last()

            val snackbarHostState = remember { SnackbarHostState() }
            var lastBackAt by remember { mutableStateOf(0L) }

            BackHandler {
                if (pop()) return@BackHandler
                val now = SystemClock.elapsedRealtime()
                if (now - lastBackAt < 1800L) finish()
                else {
                    lastBackAt = now
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(
                            message = strings.exitHint,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
                Scaffold(
                    modifier = Modifier,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    when (mode) {
                                        AppMode.EARTH -> strings.titleEarth
                                        AppMode.SUN -> strings.titleSun
                                    }
                                )
                            },
                            actions = {
                                IconButton(onClick = { push(Screen.TUTORIAL) }) {
                                    Icon(Icons.Default.MenuBook, contentDescription = strings.tutorial)
                                }

                                ModeToggleRuneButton(
                                    mode = mode,
                                    onToggle = {
                                        mode = if (mode == AppMode.EARTH) AppMode.SUN else AppMode.EARTH
                                        store.setMode(mode)
                                        setRoot(Screen.rootFor(mode))
                                    }
                                )

                                IconButton(onClick = { push(Screen.SETTINGS) }) {
                                    Icon(Icons.Default.Settings, contentDescription = strings.settings)
                                }
                            }
                        )
                    }
                ) { pad ->
                    AnimatedContent(
                        targetState = Triple(mode, current, lang),
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                        label = "nav"
                    ) { (m, s, _) ->
                        when (s) {
                            Screen.EARTH_HOME -> NowScreen(
                                vm = vm,
                                mode = AppMode.EARTH,
                                strings = strings,
                                contentPadding = pad,
                                onOpenGraphs = { push(Screen.EARTH_GRAPHS) },
                                onOpenEvents = { push(Screen.EARTH_EVENTS) }
                            )

                            Screen.EARTH_GRAPHS -> GraphsScreen(
                                title = strings.graphsTitle24h,
                                series = vm.simpleGraphSeries(),
                                mode = GraphsMode.EARTH,
                                strings = strings,
                                contentPadding = pad,
                                onClose = { pop() }
                            )

                            Screen.EARTH_EVENTS -> EventsScreen(
                                vm = vm,
                                strings = strings,
                                contentPadding = pad,
                                onClose = { pop() }
                            )

                            Screen.SUN_HOME -> SunScreen(
                                strings = strings,
                                contentPadding = pad,
                                onOpenFull = { url, title -> push(Screen.FULL(url, title)) }
                            )

                            Screen.SETTINGS -> SettingsScreen(
                                strings = strings,
                                contentPadding = pad,
                                currentLanguage = lang,
                                onSetLanguage = {
                                    lang = it
                                    store.setLanguage(it)
                                },
                                onClose = { pop() }
                            )

                            Screen.TUTORIAL -> TutorialScreen(strings = strings, contentPadding = pad, onClose = { pop() })

                            is Screen.FULL -> FullscreenWebImageScreen(
                                url = s.url,
                                title = s.title,
                                strings = strings,
                                contentPadding = pad,
                                onClose = { pop() }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class AppMode { EARTH, SUN }

sealed class Screen {
    data object EARTH_HOME : Screen()
    data object EARTH_GRAPHS : Screen()
    data object EARTH_EVENTS : Screen()
    data object SUN_HOME : Screen()
    data object SETTINGS : Screen()
    data object TUTORIAL : Screen()
    data class FULL(val url: String, val title: String) : Screen()

    companion object {
        fun rootFor(mode: AppMode): Screen = if (mode == AppMode.EARTH) EARTH_HOME else SUN_HOME
    }
}

private class SimpleFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <R : androidx.lifecycle.ViewModel> create(modelClass: Class<R>): R {
        @Suppress("UNCHECKED_CAST")
        return creator() as R
    }
}
