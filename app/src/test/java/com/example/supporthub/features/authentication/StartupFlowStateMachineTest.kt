package com.example.supporthub.features.authentication

import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.utils.StartupDestination
import com.example.supporthub.features.authentication.utils.StartupFlowState
import com.example.supporthub.features.authentication.utils.StartupFlowStateMachine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupFlowStateMachineTest {

    @Test
    fun `launch starts on visible splash while auth is loading`() {
        val state = StartupFlowStateMachine.initial()

        assertTrue(state.showSplash)
        assertTrue(state.isLoading)
        assertNull(state.navigationTarget)
        assertEquals(StartupFlowState.AwaitingStartupData, state.phase)
    }

    @Test
    fun `startup data ready keeps splash visible until splash is marked finished`() {
        val resolved = StartupFlowStateMachine.onStartupDataLoaded(
            current = StartupFlowStateMachine.initial(),
            destination = StartupDestination.Login,
            user = null
        )

        assertTrue(resolved.showSplash)
        assertFalse(resolved.isLoading)
        assertNull(resolved.navigationTarget)
        assertEquals(StartupDestination.Login, resolved.resolvedDestination)

        val splashFinished = StartupFlowStateMachine.onSplashAnimationFinished(resolved)

        assertNull(splashFinished.navigationTarget)
        assertTrue(splashFinished.showSplash)
        assertEquals(StartupFlowState.SplashReady, splashFinished.phase)

        val readyToNavigate = StartupFlowStateMachine.onMinimumSplashDurationElapsed(splashFinished)

        assertEquals(StartupDestination.Login, readyToNavigate.navigationTarget)
        assertTrue(readyToNavigate.showSplash)
        assertEquals(StartupFlowState.ReadyToNavigate, readyToNavigate.phase)
    }

    @Test
    fun `splash finishing before startup data does not navigate early`() {
        val splashFinished = StartupFlowStateMachine.onSplashAnimationFinished(
            StartupFlowStateMachine.initial()
        )

        assertTrue(splashFinished.showSplash)
        assertTrue(splashFinished.isLoading)
        assertNull(splashFinished.navigationTarget)
        assertEquals(StartupFlowState.AwaitingStartupData, splashFinished.phase)
    }

    @Test
    fun `navigation target is emitted exactly once`() {
        val resolved = StartupFlowStateMachine.onStartupDataLoaded(
            current = StartupFlowStateMachine.initial(),
            destination = StartupDestination.EmployeeDashboard,
            user = activeEmployee()
        )
        val ready = StartupFlowStateMachine.onMinimumSplashDurationElapsed(
            StartupFlowStateMachine.onSplashAnimationFinished(resolved)
        )

        assertEquals(StartupDestination.EmployeeDashboard, ready.navigationTarget)

        val consumed = StartupFlowStateMachine.onNavigationConsumed(ready)

        assertNull(consumed.navigationTarget)
        assertFalse(consumed.showSplash)
        assertEquals(StartupFlowState.Navigated, consumed.phase)

        val consumedAgain = StartupFlowStateMachine.onNavigationConsumed(consumed)

        assertNull(consumedAgain.navigationTarget)
        assertFalse(consumedAgain.showSplash)
        assertEquals(StartupFlowState.Navigated, consumedAgain.phase)
    }

    @Test
    fun `refresh after navigation can resolve a new destination without leaving navigated state unstable`() {
        val firstPass = StartupFlowStateMachine.onNavigationConsumed(
            StartupFlowStateMachine.onMinimumSplashDurationElapsed(
                StartupFlowStateMachine.onSplashAnimationFinished(
                    StartupFlowStateMachine.onStartupDataLoaded(
                        current = StartupFlowStateMachine.initial(),
                        destination = StartupDestination.PendingApproval,
                        user = pendingUser()
                    )
                )
            )
        )

        val refreshed = StartupFlowStateMachine.onStartupDataLoaded(
            current = firstPass,
            destination = StartupDestination.AgentDashboard,
            user = activeAgent()
        )

        assertEquals(StartupDestination.AgentDashboard, refreshed.navigationTarget)
        assertFalse(refreshed.isLoading)
        assertTrue(refreshed.showSplash.not())
        assertEquals(StartupFlowState.ReadyToNavigate, refreshed.phase)
    }

    @Test
    fun `logout flow should expose login as a fresh navigation target`() {
        val loggedOut = StartupFlowStateMachine.onMinimumSplashDurationElapsed(
            StartupFlowStateMachine.onSplashAnimationFinished(
                StartupFlowStateMachine.onStartupDataLoaded(
                    current = StartupFlowStateMachine.initial(),
                    destination = StartupDestination.Login,
                    user = null
                )
            )
        )

        assertEquals(StartupDestination.Login, loggedOut.navigationTarget)
        assertEquals(StartupFlowState.ReadyToNavigate, loggedOut.phase)
        assertTrue(loggedOut.showSplash)
        assertFalse(loggedOut.isLoading)
    }

    private fun pendingUser(): User = User(
        uid = "pending",
        fullName = "Pending User",
        email = "pending@example.com",
        workspaceName = "Acme",
        requestedRole = "employee",
        approvedRole = "employee",
        status = "pending"
    )

    private fun activeEmployee(): User = User(
        uid = "employee",
        fullName = "Employee User",
        email = "employee@example.com",
        workspaceName = "Acme",
        requestedRole = "employee",
        approvedRole = "employee",
        status = "active"
    )

    private fun activeAgent(): User = User(
        uid = "agent",
        fullName = "Agent User",
        email = "agent@example.com",
        workspaceName = "Acme",
        requestedRole = "agent",
        approvedRole = "agent",
        status = "active"
    )

    private fun activeAdmin(): User = User(
        uid = "admin",
        fullName = "Admin User",
        email = "admin@example.com",
        workspaceName = "Acme",
        requestedRole = "admin",
        approvedRole = "admin",
        status = "active"
    )

    @Test
    fun `active admin user resolves to admin dashboard through startup flow`() {
        val resolved = StartupFlowStateMachine.onStartupDataLoaded(
            current = StartupFlowStateMachine.initial(),
            destination = StartupDestination.AdminDashboard,
            user = activeAdmin()
        )

        val ready = StartupFlowStateMachine.onMinimumSplashDurationElapsed(
            StartupFlowStateMachine.onSplashAnimationFinished(resolved)
        )

        assertEquals(StartupDestination.AdminDashboard, ready.navigationTarget)
    }
}
