package com.github.mikan.sample.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data object Home

@Serializable
data object Cart

@Serializable
data object ItemGraph

@Serializable
data object Items

@Serializable
data class Item(val id: String, val name: String)

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Home Screen",
            style = MaterialTheme.typography.titleLarge
        )
        Button({ onNavigateToCart() }) {
            Text("Go to Cart")
        }
        Button({ onNavigateToItem() }) {
            Text("Go to Item")
        }
    }
}

@Composable
fun CartScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Text(
            text = "Cart Screen",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun ItemsScreen(
    onNavigateToItem: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Column {
            Text(
                text = "Items Screen",
                style = MaterialTheme.typography.titleLarge
            )
            List(10) {
                TextButton({ onNavigateToItem("$it") }) {
                    Text("Item $it")
                }
            }
        }
    }
}

@Composable
fun ItemScreen(name: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Green)
    ) {
        Text(
            text = "Item Screen: $name",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

sealed class TabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    open val isNew: Boolean,
)

data object HomeItem : TabItem(
    title = "Home",
    selectedIcon = Icons.Filled.Home,
    unselectedIcon = Icons.Outlined.Home,
    isNew = false,
)

data class ProfileItem(
    override val isNew: Boolean,
) : TabItem(
    title = "Profile",
    selectedIcon = Icons.Filled.Person,
    unselectedIcon = Icons.Outlined.Person,
    isNew = isNew,
)


@Composable
fun BottomNavigationBar(
    onClick: (TabItem) -> Unit,
    tabItems: List<TabItem>,
    currentTabItem: TabItem,
) {
    NavigationBar {
        tabItems.forEach { item ->
            NavigationBarItem(
                selected = item == currentTabItem,
                onClick = { onClick(item) },
                icon = {
                    TabBarIcon(
                        icon = if (item == currentTabItem) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        isNew = item.isNew,
                    )
                },
                label = {
                    Text(item.title)
                },
            )
        }
    }
}

@Composable
fun TabBarIcon(
    icon: ImageVector,
    isNew: Boolean,
) {
    BadgedBox(
        badge = {
            if (isNew) {
                Badge()
            }
        }
    ) {
        Icon(icon, contentDescription = null)
    }
}

@Preview
@Composable
private fun TabBarIconPreview() {
    TabBarIcon(
        icon = Icons.Filled.Home,
        isNew = true,
    )
}

fun NavHostController.navigateToTab(item: TabItem) {
    when (item) {
        is HomeItem -> navigate(Home)
        is ProfileItem -> navigate(Cart)
    }
}

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navItems = listOf(
        HomeItem,
        ProfileItem(isNew = true),
    )
    val navController = rememberNavController()
    val backStackEntryRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val backStackEntryFlow = navController.currentBackStackEntryFlow
    val lastEntryRoute = remember(backStackEntryRoute) { backStackEntryRoute ?: "Home" }
    var currentTabItem: TabItem by remember { mutableStateOf(HomeItem) }
    LaunchedEffect(Unit) {
        backStackEntryFlow.collect {
            println(
                """BackStackEntry(
    id: ${it.id}
    destination: ${it.destination}
    arguments: ${it.arguments}
)"""
            )
        }
    }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onClick = {
                    currentTabItem = it
                    navController.navigateToTab(it)
                },
                tabItems = navItems,
                currentTabItem = currentTabItem,
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.padding(padding),
        ) {
            composable<Home> {
                HomeScreen(
                    onNavigateToCart = {
                        navController.navigate(Cart)
                    },
                    onNavigateToItem = {
                        navController.navigate(ItemGraph)
                    },
                )
            }

            composable<Cart> {
                CartScreen()
            }

            navigation<ItemGraph>(Items) {
                composable<Items> {
                    ItemsScreen(
                        onNavigateToItem = { id ->
                            navController.navigate(Item(id, "奈良漬"))
                        },
                    )
                }

                composable<Item> { backStackEntry ->
                    val item: Item = backStackEntry.toRoute()
                    ItemScreen(item.name, modifier)
                }
            }
        }
    }
}