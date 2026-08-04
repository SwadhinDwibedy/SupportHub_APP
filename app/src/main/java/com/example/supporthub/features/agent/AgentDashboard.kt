package com.example.supporthub.features.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.supporthub.features.dashboard.components.SupportHubBottomBar

@Composable
fun AgentDashboard() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AgentNavRoutes.Home

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            SupportHubBottomBar(
                items = agentBottomNavItems,
                selectedRoute = currentRoute,
                onItemSelected = { item ->
                    if (item.route != currentRoute) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(AgentNavRoutes.Home) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AgentNavRoutes.Home
            ) {
                composable(AgentNavRoutes.Home) {
                    AgentHomeScreen()
                }
                composable(AgentNavRoutes.Queue) {
                    AgentQueueScreen()
                }
                composable(AgentNavRoutes.Chat) {
                    AgentChatScreen()
                }
                composable(AgentNavRoutes.Performance) {
                    AgentPerformanceScreen()
                }
                composable(AgentNavRoutes.Profile) {
                    AgentProfileScreen(navController = navController)
                }
            }
        }
    }
}
