package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val store = remember { GameStore(applicationContext) }
                AppNav(store)
            }
        }
    }
}

private sealed class Screen(val route: String) {
    data object Menu : Screen("menu")
    data object Game : Screen("game")
    data object Shop : Screen("shop")
    data object About : Screen("about")
}

@Composable
private fun AppNav(store: GameStore) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Screen.Menu.route) {
        composable(Screen.Menu.route) { MenuScreen(
            onPlay = { nav.navigate(Screen.Game.route) },
            onShop = { nav.navigate(Screen.Shop.route) },
            onAbout = { nav.navigate(Screen.About.route) }
        ) }
        composable(Screen.Game.route) { GameScreen(store = store, onBack = { nav.popBackStack() }) }
        composable(Screen.Shop.route) { ShopScreen(store = store, onBack = { nav.popBackStack() }) }
        composable(Screen.About.route) { AboutScreen(onBack = { nav.popBackStack() }) }
    }
}

@Composable
private fun MenuScreen(
    onPlay: () -> Unit,
    onShop: () -> Unit,
    onAbout: () -> Unit
) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Witty Clicker", style = MaterialTheme.typography.headlineMedium)
            Text("Кликер-игра: кликай, покупай улучшения, расти.", style = MaterialTheme.typography.bodyLarge)

            Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) { Text("Играть") }
            OutlinedButton(onClick = onShop, modifier = Modifier.fillMaxWidth()) { Text("Магазин") }
            TextButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) { Text("О приложении") }
        }
    }
}

@Composable
private fun GameScreen(store: GameStore, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val state by store.state.collectAsState(initial = GameState())

    // Автокликер: добавляет монеты каждую секунду
    LaunchedEffect(state.autoPerSec) {
        while (true) {
            delay(1000)
            val add = state.autoPerSec.toLong()
            if (add > 0) store.addCoins(add)
        }
    }

    Surface {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Игра", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onBack) { Text("Назад") }
            }

            Card {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Монеты: ${state.coins}", style = MaterialTheme.typography.headlineSmall)
                    Text("Сила клика: +${state.power}")
                    Text("Авто/сек: +${state.autoPerSec}")
                }
            }

            Button(
                onClick = { scope.launch { store.addCoins(state.power.toLong()) } },
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text("КЛИК  (+${state.power})")
            }

            OutlinedButton(
                onClick = { scope.launch { store.reset() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить прогресс")
            }
        }
    }
}

@Composable
private fun ShopScreen(store: GameStore, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val state by store.state.collectAsState(initial = GameState())

    // Цена растёт с каждым апгрейдом
    val powerCost = (25L * state.power).coerceAtMost(999_999L)
    val autoCost = (100L * (state.autoPerSec + 1)).coerceAtMost(999_999L)

    var toast by remember { mutableStateOf<String?>(null) }

    Surface {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Магазин", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onBack) { Text("Назад") }
            }

            Card {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Монеты: ${state.coins}", style = MaterialTheme.typography.titleLarge)
                    Text("Сила клика: ${state.power}")
                    Text("Авто/сек: ${state.autoPerSec}")
                }
            }

            ElevatedCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Улучшение: Сила клика +1")
                    Text("Цена: $powerCost")
                    Button(
                        onClick = {
                            scope.launch {
                                if (store.spendCoins(powerCost)) {
                                    store.upgradePower()
                                    toast = "Клик стал сильнее!"
                                } else toast = "Не хватает монет"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Купить") }
                }
            }

            ElevatedCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Улучшение: Автокликер +1/сек")
                    Text("Цена: $autoCost")
                    Button(
                        onClick = {
                            scope.launch {
                                if (store.spendCoins(autoCost)) {
                                    store.upgradeAuto()
                                    toast = "Автокликер усилен!"
                                } else toast = "Не хватает монет"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Купить") }
                }
            }

            toast?.let { msg ->
                AssistChip(
                    onClick = { toast = null },
                    label = { Text(msg) }
                )
            }
        }
    }
}

@Composable
private fun AboutScreen(onBack: () -> Unit) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("О приложении", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onBack) { Text("Назад") }
            }

            Text("Witty Clicker — минимальный кликер на Jetpack Compose.")
            Text("Планы: больше апгрейдов, достижения, оффлайн-прогресс, звуки/вибрация.")
        }
    }
}