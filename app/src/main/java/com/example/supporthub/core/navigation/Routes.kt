package com.example.supporthub.core.navigation

sealed class Routes(val route: String) {

    data object Splash : Routes("splash")

    object Login : Routes("login")

    object Register : Routes("register")

    object ForgotPassword : Routes("forgot_password")

    object CompleteProfile : Routes("complete_profile")

    object PendingApproval : Routes("pending_approval")

    object WorkspaceLoading : Routes("workspace_loading")

    object EmployeeDashboard : Routes("employee_dashboard")

    object AgentDashboard : Routes("agent_dashboard")

    object AdminDashboard : Routes("admin_dashboard")
}