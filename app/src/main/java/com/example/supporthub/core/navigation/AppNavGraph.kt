package com.example.supporthub.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.supporthub.features.admin.AdminDashboard
import com.example.supporthub.features.admin.AdminUserDetailScreen
import com.example.supporthub.features.admin.FirebaseAdminUserDetailRepository
import com.example.supporthub.features.agent.AgentDashboard
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.authentication.screens.CompleteProfileScreen
import com.example.supporthub.features.authentication.screens.ForgotPasswordScreen
import com.example.supporthub.features.authentication.screens.LoginScreen
import com.example.supporthub.features.authentication.screens.PendingApprovalScreen
import com.example.supporthub.features.authentication.screens.RegisterScreen
import com.example.supporthub.features.authentication.screens.WorkspaceLoadingScreen
import com.example.supporthub.features.authentication.utils.StartupDestination
import com.example.supporthub.features.authentication.viewmodel.AuthViewModel
import com.example.supporthub.features.authentication.viewmodel.AuthViewModelFactory
import com.example.supporthub.features.employee.EmployeeDashboard
import com.example.supporthub.features.splash.SplashScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            AuthRepositoryImpl()
        )
    )
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.onSplashAnimationFinished()
        kotlinx.coroutines.delay(1000)
        authViewModel.onMinimumSplashDurationElapsed()
    }

    LaunchedEffect(uiState.startupFlow.navigationTarget) {
        val destination = uiState.startupFlow.navigationTarget ?: return@LaunchedEffect
        val targetRoute = when (destination) {
            StartupDestination.Login -> Routes.Login.route
            StartupDestination.CompleteProfile -> Routes.CompleteProfile.route
            StartupDestination.PendingApproval -> Routes.PendingApproval.route
            StartupDestination.EmployeeDashboard -> Routes.EmployeeDashboard.route
            StartupDestination.AgentDashboard -> Routes.AgentDashboard.route
            StartupDestination.AdminDashboard -> Routes.AdminDashboard.route
        }

        if (navController.currentDestination?.route != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(Routes.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }

        authViewModel.consumeStartupDestination()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {

        composable(Routes.Splash.route) {
            SplashScreen()
        }

        composable(Routes.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.WorkspaceLoading.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onPendingApproval = {
                    navController.navigate(Routes.PendingApproval.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoogleCompleteProfile = {
                    navController.navigate(Routes.CompleteProfile.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.Register.route) {
                        launchSingleTop = true
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate(Routes.ForgotPassword.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.WorkspaceLoading.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onPendingApproval = {
                    navController.navigate(Routes.PendingApproval.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoogleCompleteProfile = {
                    navController.navigate(Routes.CompleteProfile.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PendingApproval.route) {
            uiState.user?.let { user ->
                PendingApprovalScreen(
                    user = user,
                    onRefresh = {
                        authViewModel.currentUser()
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Routes.WorkspaceLoading.route) {
            uiState.user?.let { user ->
                WorkspaceLoadingScreen(
                    user = user,
                    onFinished = { destination ->
                        val targetRoute = when (destination) {
                            "pending" -> Routes.PendingApproval.route
                            "employee" -> Routes.EmployeeDashboard.route
                            "agent" -> Routes.AgentDashboard.route
                            "admin" -> Routes.AdminDashboard.route
                            else -> Routes.Login.route
                        }

                        if (navController.currentDestination?.route != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(Routes.WorkspaceLoading.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            } ?: run {
                navController.navigate(Routes.Login.route) {
                    popUpTo(Routes.WorkspaceLoading.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        composable(Routes.EmployeeDashboard.route) {
            EmployeeDashboard()
        }

        composable(Routes.AgentDashboard.route) {
            AgentDashboard()
        }

        composable(Routes.AdminDashboard.route) {
            AdminDashboard(
                onLoggedOut = {
                    authViewModel.logout()
                    navController.navigate(Routes.Splash.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onUserSelected = { user ->
                    val encodedUid = URLEncoder.encode(user.uid, StandardCharsets.UTF_8.name())
                    navController.navigate("admin_user_detail/$encodedUid")
                }
            )
        }

        composable(
            route = "admin_user_detail/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid").orEmpty()
            AdminUserDetailScreen(
                uid = URLDecoder.decode(uid, StandardCharsets.UTF_8.name()),
                onNavigateBack = { navController.popBackStack() },
                repository = FirebaseAdminUserDetailRepository()
            )
        }

        composable(Routes.CompleteProfile.route) {
            if (uiState.user != null) {
                CompleteProfileScreen(
                    viewModel = authViewModel,
                    onCompleted = {
                        navController.navigate(Routes.WorkspaceLoading.route) {
                            popUpTo(Routes.CompleteProfile.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

    }
}
