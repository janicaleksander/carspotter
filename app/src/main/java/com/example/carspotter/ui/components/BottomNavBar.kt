package com.example.carspotter.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.carspotter.navigation.Screen
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.ui.theme.Neutral10
import com.example.carspotter.ui.theme.Neutral90
import com.example.carspotter.ui.theme.NeutralWhite
import com.example.carspotter.ui.theme.TopsOrange

@Composable
fun BottomNavBar(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screens = listOf(
        Screen.Home,
        Screen.Garage,
        Screen.New,
        Screen.Tops,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = NeutralWhite,
        modifier = Modifier.height(if (isLandscape) 56.dp else 80.dp),
    ) {
        screens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val isTops = screen is Screen.Tops
            val activeColor = if (isTops) TopsOrange else CarRed

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.size(if (isLandscape) 22.dp else 24.dp),
                    )
                },
                label = if (isLandscape) null else ({
                    Text(
                        text = screen.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }),
                alwaysShowLabel = !isLandscape,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    unselectedIconColor = Neutral10.copy(alpha = 0.38f),
                    unselectedTextColor = Neutral10.copy(alpha = 0.38f),
                    indicatorColor = activeColor.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
