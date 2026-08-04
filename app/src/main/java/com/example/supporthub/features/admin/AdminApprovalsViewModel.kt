package com.example.supporthub.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminApprovalsUiState(
    val isLoading: Boolean = false,
    val workspaceUsers: List<User> = emptyList(),
    val pendingUsers: List<User> = emptyList(),
    val errorMessage: String? = null,
    val actionMessage: String? = null
)

class AdminApprovalsViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminApprovalsUiState(isLoading = true))
    val uiState: StateFlow<AdminApprovalsUiState> = _uiState.asStateFlow()

    init {
        loadPendingUsers()
    }

    fun loadPendingUsers() {
        refreshWorkspaceUsers()
    }

    private fun refreshWorkspaceUsers(actionMessage: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                actionMessage = null
            )

            val currentUser = repository.getCurrentUser()
            val workspaceName = currentUser?.workspaceName?.trim().orEmpty()
            if (workspaceName.isBlank()) {
                _uiState.value = AdminApprovalsUiState(
                    isLoading = false,
                    errorMessage = "Workspace users could not be loaded."
                )
                return@launch
            }

            repository.getWorkspaceUsers(workspaceName).fold(
                onSuccess = { users ->
                    _uiState.value = AdminApprovalsUiState(
                        isLoading = false,
                        workspaceUsers = users,
                        pendingUsers = users.filter { it.status.equals("pending", ignoreCase = true) },
                        actionMessage = actionMessage
                    )
                },
                onFailure = { error ->
                    _uiState.value = AdminApprovalsUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load workspace users."
                    )
                }
            )
        }
    }

    fun approveUser(
        uid: String,
        role: Role
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                actionMessage = null
            )

            repository.approveUser(uid, role).fold(
                onSuccess = {
                    refreshWorkspaceUsers(actionMessage = "Approved user as ${role.value}.")
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to approve user."
                    )
                }
            )
        }
    }

    fun keepPending(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                actionMessage = null
            )

            repository.rejectUser(uid).fold(
                onSuccess = {
                    refreshWorkspaceUsers(actionMessage = "User remains pending approval.")
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to update user status."
                    )
                }
            )
        }
    }
}

class AdminApprovalsViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminApprovalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminApprovalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
