package com.example.supporthub.features.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.supporthub.core.demo.DemoDataProvider
import com.example.supporthub.core.demo.USE_DEMO_DATA
import com.example.supporthub.features.authentication.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmployeeProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLogoutSuccess: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val jobTitle: String = "",
    val department: String = "",
    val workspaceName: String = "",
    val avatarInitials: String = "",
    val openTickets: Int = 0,
    val resolvedTickets: Int = 0,
    val rating: String = "—",
    val fullNameError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class EmployeeProfileViewModel(
    private val repository: EmployeeProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeProfileUiState(isLoading = true))
    val uiState: StateFlow<EmployeeProfileUiState> = _uiState.asStateFlow()

    private var loadedUser: User? = null
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
                successMessage = null,
                isLogoutSuccess = false
            )

            repository.observeProfile().collect { profileData ->
                val resolvedProfile = profileData ?: DemoDataProvider.employeeProfileData()
                loadedUser = resolvedProfile.user
                val keepSuccessMessage = _uiState.value.successMessage?.takeIf { !_uiState.value.isSaving }
                _uiState.value = resolvedProfile.toProfileUiState(
                    isLoading = false,
                    isSaving = _uiState.value.isSaving,
                    isLoggingOut = _uiState.value.isLoggingOut,
                    isLogoutSuccess = false,
                    successMessage = keepSuccessMessage
                )
            }
        }
    }

    fun logout() {
        if (_uiState.value.isLoggingOut || _uiState.value.isLogoutSuccess) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoggingOut = true,
                errorMessage = null,
                successMessage = null
            )

            stopProfileObservation()
            loadedUser = null

            runCatching {
                if (!USE_DEMO_DATA) {
                    repository.logout()
                }
            }.onSuccess {
                _uiState.value = EmployeeProfileUiState(
                    isLogoutSuccess = true
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    errorMessage = error.message ?: "Unable to logout."
                )
            }
        }
    }

    override fun onCleared() {
        stopProfileObservation()
        super.onCleared()
    }

    private fun stopProfileObservation() {
        observeProfileJob?.cancel()
        observeProfileJob = null
    }

    fun onFullNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            fullName = value,
            fullNameError = null,
            errorMessage = null,
            successMessage = null,
            avatarInitials = buildAvatarInitials(value, _uiState.value.email)
        )
    }

    fun onPhoneChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            phone = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onLocationChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            location = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onJobTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            jobTitle = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onDepartmentChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            department = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun saveProfile() {
        val baseUser = loadedUser
        if (baseUser == null) {
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                errorMessage = "Unable to load employee profile."
            )
            return
        }

        val trimmedName = _uiState.value.fullName.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                fullNameError = "Full name is required.",
                isSaving = false,
                errorMessage = null,
                successMessage = null
            )
            return
        }

        val updatedUser = baseUser.copy(
            fullName = trimmedName,
            phone = _uiState.value.phone.trim(),
            location = _uiState.value.location.trim(),
            jobTitle = _uiState.value.jobTitle.trim(),
            department = _uiState.value.department.trim(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                fullNameError = null,
                errorMessage = null,
                successMessage = null
            )

            runCatching { repository.updateUserProfile(updatedUser) }
                .onSuccess { result ->
                    val savedUser = result.getOrNull() ?: updatedUser
                    loadedUser = savedUser
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        successMessage = "Profile updated successfully.",
                        errorMessage = null,
                        fullName = savedUser.fullName,
                        email = savedUser.email,
                        phone = savedUser.phone,
                        location = savedUser.location,
                        jobTitle = savedUser.jobTitle,
                        department = savedUser.department,
                        avatarInitials = buildAvatarInitials(savedUser.fullName, savedUser.email)
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Unable to update profile.",
                        successMessage = null
                    )
                }
        }
    }
}

class EmployeeProfileViewModelFactory(
    private val repository: EmployeeProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployeeProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmployeeProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun EmployeeProfileData.toProfileUiState(
    isLoading: Boolean,
    isSaving: Boolean,
    isLoggingOut: Boolean,
    isLogoutSuccess: Boolean,
    successMessage: String? = null
): EmployeeProfileUiState {
    return EmployeeProfileUiState(
        isLoading = isLoading,
        isSaving = isSaving,
        isLoggingOut = isLoggingOut,
        isLogoutSuccess = isLogoutSuccess,
        fullName = user.fullName,
        email = user.email,
        phone = user.phone,
        location = user.location,
        jobTitle = user.jobTitle,
        department = user.department,
        workspaceName = user.workspaceName,
        avatarInitials = buildAvatarInitials(user.fullName, user.email),
        openTickets = metrics.openTickets,
        resolvedTickets = metrics.resolvedTickets,
        rating = metrics.rating,
        errorMessage = null,
        successMessage = successMessage
    )
}

private fun buildAvatarInitials(fullName: String, email: String): String {
    val words = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
        words.size == 1 -> words[0].take(2).uppercase()
        email.isNotBlank() -> email.trim().first().toString().uppercase()
        else -> "?"
    }
}
