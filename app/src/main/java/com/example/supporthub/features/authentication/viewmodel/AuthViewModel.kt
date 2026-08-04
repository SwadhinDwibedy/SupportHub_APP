package com.example.supporthub.features.authentication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.utils.AuthDestination
import com.example.supporthub.features.authentication.utils.AuthUiState
import com.example.supporthub.features.authentication.utils.StartupDestination
import com.example.supporthub.features.authentication.utils.StartupDestinationResolver
import com.example.supporthub.features.authentication.utils.StartupFlowStateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(isLoading = true))

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    init {
        resolveStartupDestination()
    }

    fun resolveStartupDestination() {
        if (_uiState.value.startupFlow.isLoading) {
            viewModelScope.launch {
                val startupUser = repository.resolveStartupUser()
                val startupDestination = StartupDestinationResolver.resolve(startupUser)
                val startupFlow = StartupFlowStateMachine.onStartupDataLoaded(
                    current = _uiState.value.startupFlow,
                    destination = startupDestination,
                    user = startupUser
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = startupUser,
                    startupDestination = startupFlow.navigationTarget,
                    startupFlow = startupFlow,
                    error = null
                )
            }
        }
    }

    fun onSplashAnimationFinished() {
        val startupFlow = StartupFlowStateMachine.onSplashAnimationFinished(_uiState.value.startupFlow)
        _uiState.value = _uiState.value.copy(
            startupDestination = startupFlow.navigationTarget,
            startupFlow = startupFlow
        )
    }

    fun onMinimumSplashDurationElapsed() {
        val startupFlow = StartupFlowStateMachine.onMinimumSplashDurationElapsed(_uiState.value.startupFlow)
        _uiState.value = _uiState.value.copy(
            startupDestination = startupFlow.navigationTarget,
            startupFlow = startupFlow
        )
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        workspaceName: String,
        role: Role
    ) {

        viewModelScope.launch {

            _uiState.value = AuthUiState(
                isLoading = true,
                user = _uiState.value.user
            )

            val result = repository.registerUser(
                fullName,
                email,
                password,
                workspaceName,
                role
            )

            result.fold(

                onSuccess = {

                    _uiState.value = successStateFor(it)

                },

                onFailure = {

                    _uiState.value = AuthUiState(
                        user = _uiState.value.user,
                        error = it.message
                    )

                }

            )

        }

    }

    fun login(
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            _uiState.value = AuthUiState(
                isLoading = true,
                user = _uiState.value.user
            )

            val result = repository.loginUser(
                email,
                password
            )

            result.fold(

                onSuccess = {

                    _uiState.value = successStateFor(it)

                },

                onFailure = {

                    _uiState.value = AuthUiState(
                        error = it.message
                    )

                }

            )

        }

    }

    fun googleLogin(
        idToken: String
    ) {

        viewModelScope.launch {

            _uiState.value = AuthUiState(
                isLoading = true,
                user = _uiState.value.user
            )

            val result = repository.signInWithGoogle(
                idToken
            )

            result.fold(

                onSuccess = {

                    _uiState.value = successStateFor(it)

                },

                onFailure = {

                    _uiState.value = AuthUiState(
                        error = it.message
                    )

                }

            )

        }

    }

    fun sendResetEmail(
        email: String
    ) {

        viewModelScope.launch {

            repository.sendPasswordResetEmail(email)

        }

    }

    fun logout() {

        viewModelScope.launch {

            repository.logout()

            val startupFlow = StartupFlowStateMachine.onMinimumSplashDurationElapsed(
                StartupFlowStateMachine.onSplashAnimationFinished(
                    StartupFlowStateMachine.onStartupDataLoaded(
                        current = StartupFlowStateMachine.initial(),
                        destination = StartupDestination.Login,
                        user = null
                    )
                )
            )

            _uiState.value = AuthUiState(
                isLoading = false,
                startupDestination = startupFlow.navigationTarget,
                startupFlow = startupFlow
            )

        }

    }

    fun currentUser() {

        viewModelScope.launch {

            val user = repository.getCurrentUser()
            val destination = StartupDestinationResolver.resolve(user)
            val startupFlow = StartupFlowStateMachine.onNavigationConsumed(
                StartupFlowStateMachine.onMinimumSplashDurationElapsed(
                    StartupFlowStateMachine.onSplashAnimationFinished(
                        StartupFlowStateMachine.onStartupDataLoaded(
                            current = StartupFlowStateMachine.initial(),
                            destination = destination,
                            user = user
                        )
                    )
                )
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                user = user,
                startupDestination = startupFlow.navigationTarget,
                startupFlow = startupFlow
            )

        }

    }

    fun completeGoogleProfile(
        fullName: String,
        email: String,
        workspaceName: String,
        role: Role
    ){

        viewModelScope.launch {

            _uiState.value = AuthUiState(
                isLoading = true,
                user = _uiState.value.user
            )

            val result = repository.completeGoogleProfile(
                fullName,
                email,
                workspaceName,
                role
            )

            result.fold(

                onSuccess = {

                    _uiState.value = successStateFor(it)

                },

                onFailure = {

                    _uiState.value = AuthUiState(
                        user = _uiState.value.user,
                        error = it.message
                    )

                }

            )

        }

    }

    fun consumeDestination() {
        _uiState.value = _uiState.value.copy(
            success = false,
            destination = null
        )
    }

    fun consumeStartupDestination() {
        val startupFlow = StartupFlowStateMachine.onNavigationConsumed(_uiState.value.startupFlow)
        _uiState.value = _uiState.value.copy(
            startupDestination = null,
            startupFlow = startupFlow
        )
    }

    private fun successStateFor(user: User): AuthUiState {
        val startupDestination = StartupDestinationResolver.resolve(user)
        val startupFlow = StartupFlowStateMachine.onNavigationConsumed(
            StartupFlowStateMachine.onMinimumSplashDurationElapsed(
                StartupFlowStateMachine.onSplashAnimationFinished(
                    StartupFlowStateMachine.onStartupDataLoaded(
                        current = StartupFlowStateMachine.initial(),
                        destination = startupDestination,
                        user = user
                    )
                )
            )
        )
        return AuthUiState(
            success = true,
            user = user,
            startupDestination = startupFlow.navigationTarget,
            startupFlow = startupFlow,
            destination = resolveDestination(user)
        )
    }

    private fun resolveDestination(user: User): AuthDestination {
        val startupDestination = StartupDestinationResolver.resolve(user)
        return when (startupDestination) {
            StartupDestination.CompleteProfile -> AuthDestination.CompleteProfile
            StartupDestination.PendingApproval -> AuthDestination.PendingApproval
            StartupDestination.EmployeeDashboard,
            StartupDestination.AgentDashboard,
            StartupDestination.AdminDashboard -> AuthDestination.WorkspaceLoading
            StartupDestination.Login -> AuthDestination.WorkspaceLoading
        }
    }

}
