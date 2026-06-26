package com.utakatalp.donebot.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

private data class BottomBarTab(
    val key: AppKey,
    val label: String,
    val icon: @Composable () -> Unit
)

private val TABS = listOf(
    BottomBarTab(Home, "Home") { Icon(Icons.Default.Home, contentDescription = "Home") },
    BottomBarTab(Profile, "Profile") { Icon(Icons.Default.Person, contentDescription = "Profile") },
)

@Composable
fun DoneBotBottomBar(
    currentRoute: NavKey,
    onTabSelected: (AppKey) -> Unit
) {
    NavigationBar {
        TABS.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.key,
                onClick = { onTabSelected(tab.key) },
                icon = tab.icon,
                label = { Text(tab.label) }
            )
        }
    }
}
