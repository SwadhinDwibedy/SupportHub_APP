package com.example.supporthub.features.admin

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
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.dashboard.components.SupportHubBottomBar

@Composable
fun AdminDashboard(
    onLoggedOut: () -> Unit = {},
    onUserSelected: (com.example.supporthub.features.authentication.model.User) -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AdminNavRoutes.Home

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            SupportHubBottomBar(
                items = adminBottomNavItems,
                selectedRoute = currentRoute,
                onItemSelected = { item ->
                    if (item.route != currentRoute) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(AdminNavRoutes.Home) {
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
                startDestination = AdminNavRoutes.Home
            ) {
                composable(AdminNavRoutes.Home) { AdminHomeScreen() }
                composable(AdminNavRoutes.Users) { AdminUsersScreen(onUserSelected = onUserSelected) }
                composable(AdminNavRoutes.Tickets) { AdminTicketsScreen() }
                composable(AdminNavRoutes.Analytics) { AdminAnalyticsScreen() }
                composable(AdminNavRoutes.Profile) {
                    AdminProfileScreen(
                        repository = FirebaseAdminProfileRepository(AuthRepositoryImpl()),
                        onLoggedOut = onLoggedOut
                    )
                }
            }
        }
    }
}
