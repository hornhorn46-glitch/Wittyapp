package com.example.wittyapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class Tab { NOW, AURORA, EVENTS }

@Composable
fun AppTabs(
    now: @Composable () -> Unit,
    aurora: @Composable () -> Unit,
    events: @Composable () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(Tab.NOW) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.NOW,
                    onClick = { tab = Tab.NOW },
                    label = { Text("Сейчас") },
                    icon = { Icon(Icons.Default.Speed, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.AURORA,
                    onClick = { tab = Tab.AURORA },
                    label = { Text("Сияния") },
                    icon = { Icon(Icons.Default.Public, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.EVENTS,
                    onClick = { tab = Tab.EVENTS },
                    label = { Text("События") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = { tween(220) togetherWith tween(220) }
            ) { t ->
                when (t) {
                    Tab.NOW -> now()
                    Tab.AURORA -> aurora()
                    Tab.EVENTS -> events()
                }
            }
        }
    }
}

// Минимальные иконки (Material Icons)
private object Icons {
    object Default {
        @Composable fun Speed() = androidx.compose.material.icons.Icons.Default.Speed
        @Composable fun Public() = androidx.compose.material.icons.Icons.Default.Public
        @Composable fun List() = androidx.compose.material.icons.Icons.Default.List
    }
}