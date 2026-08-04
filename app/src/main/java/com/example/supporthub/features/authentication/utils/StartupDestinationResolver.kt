package com.example.supporthub.features.authentication.utils

import com.example.supporthub.features.authentication.model.User

enum class StartupDestination {
    Login,
    CompleteProfile,
    PendingApproval,
    EmployeeDashboard,
    AgentDashboard,
    AdminDashboard
}

object StartupDestinationResolver {

    fun resolve(user: User?): StartupDestination {
        if (user == null) {
            return StartupDestination.Login
        }

        if (!hasCompletedProfile(user)) {
            return StartupDestination.CompleteProfile
        }

        val approvedRole = user.approvedRole?.trim().orEmpty()
        val requestedRole = user.requestedRole.trim()
        val effectiveRole = when {
            approvedRole.isNotBlank() -> approvedRole
            user.status.equals("active", ignoreCase = true) && requestedRole.isNotBlank() -> requestedRole
            else -> ""
        }

        return when {
            user.status.equals("pending", ignoreCase = true) -> StartupDestination.PendingApproval
            user.status.equals("active", ignoreCase = true) && effectiveRole.equals("employee", ignoreCase = true) -> StartupDestination.EmployeeDashboard
            user.status.equals("active", ignoreCase = true) && effectiveRole.equals("agent", ignoreCase = true) -> StartupDestination.AgentDashboard
            user.status.equals("active", ignoreCase = true) && effectiveRole.equals("admin", ignoreCase = true) -> StartupDestination.AdminDashboard
            else -> StartupDestination.Login
        }
    }

    private fun hasCompletedProfile(user: User): Boolean {
        return user.fullName.isNotBlank() &&
            user.email.isNotBlank() &&
            user.workspaceName.isNotBlank() &&
            user.requestedRole.isNotBlank()
    }
}
