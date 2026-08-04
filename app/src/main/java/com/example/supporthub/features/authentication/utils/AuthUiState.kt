package com.example.supporthub.features.authentication.utils

import com.example.supporthub.features.authentication.model.User

enum class AuthDestination {
    CompleteProfile,
    PendingApproval,
    WorkspaceLoading
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val user: User? = null,
    val startupDestination: StartupDestination? = null,
    val startupFlow: StartupFlowSnapshot = StartupFlowStateMachine.initial(),
    val destination: AuthDestination? = null,
    val error: String? = null
)
