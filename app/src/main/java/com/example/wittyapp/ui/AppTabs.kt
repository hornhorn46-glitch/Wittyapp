package com.example.wittyapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
                    icon = { Icon(Icons.Filled.Speed, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.AURORA,
                    onClick = { tab = Tab.AURORA },
                    label = { Text("Сияния") },
                    icon = { Icon(Icons.Filled.Public, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.EVENTS,
                    onClick = { tab = Tab.EVENTS },
                    label = { Text("События") },
                    icon = { Icon(Icons.Filled.List, contentDescription = null) }
                )
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
        ) {
            AnimatedContent(targetState = tab, label = "tabs") { t ->
                when (t) {
                    Tab.NOW -> now()
                    Tab.AURORA -> aurora()
                    Tab.EVENTS -> events()
                }
            }
        }
    }
}