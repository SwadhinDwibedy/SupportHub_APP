package com.example.supporthub.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.supporthub.features.authentication.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class AdminProfileUiState(
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val jobTitle: String = "",
    val department: String = "",
    val workspaceName: String = "",
    val roleLabel: String = "",
    val statusLabel: String = "",
    val statusBadgeLabel: String = "",
    val authProviderLabel: String = "",
    val avatarInitials: String = "",
    val avatarUrl: String = "",
    val employeeId: String = "",
    val joinedDateLabel: String = "",
    val availabilityMessage: String = "Available for workspace requests",
    val isAvailable: Boolean = true,
    val resolvedTicketsLabel: String = "1,240",
    val totalTicketsLabel: String = "1,240",
    val openTicketsLabel: String = "18",
    val workspaceRankLabel: String = "#12",
    val csatLabel: String = "4.9",
    val responseTimeLabel: String = "2m",
    val errorMessage: String? = null
)

class AdminProfileViewModel(
    private val repository: AdminProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProfileUiState(isLoading = true))
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    private var observeProfileJob: Job? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        observeProfileJob?.cancel()
        observeProfileJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isLoggedOut = false
            )

            repository.observeProfile().collect { profileData ->
                if (profileData == null) {
                    _uiState.value = AdminProfileUiState(
                        isLoading = false,
                        errorMessage = "Unable to load admin profile."
                    )
                } else {
                    _uiState.value = profileData.toUiState(
                        isLoading = false,
                        isLoggingOut = _uiState.value.isLoggingOut,
                        isLoggedOut = false,
                        isUpdatingProfile = _uiState.value.isUpdatingProfile
                    )
                }
            }
        }
    }

    fun updateProfile(update: AdminProfileUpdate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdatingProfile = true,
                errorMessage = null
            )
            runCatching { repository.updateProfile(update) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingProfile = false,
                        errorMessage = "Profile updated successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingProfile = false,
                        errorMessage = error.message ?: "Unable to update profile."
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoggingOut = true,
                errorMessage = null,
                isLoggedOut = false
            )

            runCatching { repository.logout() }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        isLoggedOut = true,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        isLoggedOut = false,
                        errorMessage = error.message ?: "Unable to sign out."
                    )
                }
        }
    }
}

class AdminProfileViewModelFactory(
    private val repository: AdminProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun AdminProfileData.toUiState(
    isLoading: Boolean,
    isLoggingOut: Boolean,
    isLoggedOut: Boolean,
    isUpdatingProfile: Boolean
): AdminProfileUiState {
    val resolvedRole = user.approvedRole?.takeIf { it.isNotBlank() } ?: user.requestedRole
    return AdminProfileUiState(
        isLoading = isLoading,
        isLoggingOut = isLoggingOut,
        isLoggedOut = isLoggedOut,
        isUpdatingProfile = isUpdatingProfile,
        fullName = user.fullName,
        email = user.email,
        phone = user.phone,
        location = user.location,
        jobTitle = user.jobTitle,
        department = user.department,
        workspaceName = user.workspaceName,
        roleLabel = resolvedRole.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        statusLabel = user.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        statusBadgeLabel = if (user.status.equals("active", ignoreCase = true)) "Online" else user.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        authProviderLabel = user.authProvider.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        avatarInitials = buildAdminAvatarInitials(user.fullName, user.email),
        avatarUrl = user.avatarUrl.orEmpty(),
        employeeId = user.uid.takeIf { it.isNotBlank() }?.takeLast(6)?.uppercase().orEmpty(),
        joinedDateLabel = "Joined ${user.createdAt}",
        availabilityMessage = if (user.status.equals("active", ignoreCase = true)) "Online and ready to help" else "Currently away",
        isAvailable = user.status.equals("active", ignoreCase = true),
        resolvedTicketsLabel = summary.totalTickets.takeIf { it > 0 }?.let { String.format("%,d", it) } ?: "1,240",
        totalTicketsLabel = summary.totalTickets.takeIf { it > 0 }?.let { String.format("%,d", it) } ?: "1,240",
        openTicketsLabel = summary.pendingApprovals.takeIf { it > 0 }?.let { String.format("%,d", it) } ?: "18",
        workspaceRankLabel = "#${(summary.activeAgents + 12).coerceAtLeast(1)}",
        csatLabel = "4.9",
        responseTimeLabel = "2m",
        errorMessage = null
    )
}

internal fun buildAdminAvatarInitials(fullName: String, email: String): String {
    val words = fullName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    return when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
        words.size == 1 -> words[0].take(2).uppercase()
        email.isNotBlank() -> email.trim().first().toString().uppercase()
        else -> "?"
    }
}
