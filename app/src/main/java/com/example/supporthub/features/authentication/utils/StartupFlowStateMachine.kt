package com.example.supporthub.features.authentication.utils

import com.example.supporthub.features.authentication.model.User

enum class StartupFlowState {
    AwaitingStartupData,
    SplashReady,
    ReadyToNavigate,
    Navigated
}

data class StartupFlowSnapshot(
    val phase: StartupFlowState = StartupFlowState.AwaitingStartupData,
    val isLoading: Boolean = true,
    val showSplash: Boolean = true,
    val hasSplashAnimationFinished: Boolean = false,
    val hasMetMinimumSplashDuration: Boolean = false,
    val resolvedDestination: StartupDestination? = null,
    val navigationTarget: StartupDestination? = null,
    val user: User? = null
)

object StartupFlowStateMachine {

    fun initial(): StartupFlowSnapshot = StartupFlowSnapshot()

    fun onStartupDataLoaded(
        current: StartupFlowSnapshot,
        destination: StartupDestination,
        user: User?
    ): StartupFlowSnapshot {
        val shouldNavigateImmediately =
            current.hasSplashAnimationFinished && current.hasMetMinimumSplashDuration
        return current.copy(
            phase = if (shouldNavigateImmediately) StartupFlowState.ReadyToNavigate else StartupFlowState.SplashReady,
            isLoading = false,
            showSplash = !shouldNavigateImmediately || current.showSplash,
            resolvedDestination = destination,
            navigationTarget = if (shouldNavigateImmediately) destination else null,
            user = user
        )
    }

    fun onSplashAnimationFinished(current: StartupFlowSnapshot): StartupFlowSnapshot {
        val destination = current.resolvedDestination
        val canNavigate = destination != null && current.hasMetMinimumSplashDuration
        return current.copy(
            phase = if (canNavigate) StartupFlowState.ReadyToNavigate else current.phase,
            hasSplashAnimationFinished = true,
            showSplash = true,
            navigationTarget = if (canNavigate) destination else null
        )
    }

    fun onMinimumSplashDurationElapsed(current: StartupFlowSnapshot): StartupFlowSnapshot {
        val destination = current.resolvedDestination
        val canNavigate = destination != null && current.hasSplashAnimationFinished
        return current.copy(
            phase = if (canNavigate) StartupFlowState.ReadyToNavigate else current.phase,
            hasMetMinimumSplashDuration = true,
            showSplash = true,
            navigationTarget = if (canNavigate) destination else null
        )
    }

    fun onNavigationConsumed(current: StartupFlowSnapshot): StartupFlowSnapshot {
        if (current.navigationTarget == null && current.phase == StartupFlowState.Navigated) {
            return current
        }

        return current.copy(
            phase = StartupFlowState.Navigated,
            isLoading = false,
            showSplash = false,
            navigationTarget = null
        )
    }
}
