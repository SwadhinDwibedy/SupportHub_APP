package com.example.supporthub.features.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.supporthub.core.navigation.Routes
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.authentication.utils.StartupDestination
import com.example.supporthub.features.authentication.viewmodel.AuthViewModel
import com.example.supporthub.features.authentication.viewmodel.AuthViewModelFactory
import com.example.supporthub.features.dashboard.components.SupportHubBottomBar

@Composable
fun EmployeeDashboard() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AuthRepositoryImpl())
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: EmployeeNavRoutes.Home
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authState.startupDestination) {
        if (authState.startupDestination == StartupDestination.Login) {
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.EmployeeDashboard.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            SupportHubBottomBar(
                items = employeeBottomNavItems,
                selectedRoute = currentRoute,
                onItemSelected = { item ->
                    if (item.route != currentRoute) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(EmployeeNavRoutes.Home) {
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
                startDestination = EmployeeNavRoutes.Home
            ) {
                composable(EmployeeNavRoutes.Home) { EmployeeHomeScreen(navController = navController) }
                composable(EmployeeNavRoutes.Tickets) { EmployeeTicketsScreen(navController = navController) }
                composable(
                    route = EmployeeNavRoutes.TicketDetailsRoute,
                    arguments = listOf(navArgument(EmployeeNavRoutes.TicketIdArg) { type = NavType.StringType })
                ) { backStackEntry ->
                    EmployeeTicketDetailsScreen(
                        navController = navController,
                        ticketId = backStackEntry.arguments?.getString(EmployeeNavRoutes.TicketIdArg).orEmpty()
                    )
                }
                composable(EmployeeNavRoutes.Chat) { EmployeeChatScreen() }
                composable(EmployeeNavRoutes.Profile) {
                    EmployeeProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLoggedOut = {
                            authViewModel.logout()
                            navController.navigate(Routes.Splash.route) {
                                popUpTo(Routes.EmployeeDashboard.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
