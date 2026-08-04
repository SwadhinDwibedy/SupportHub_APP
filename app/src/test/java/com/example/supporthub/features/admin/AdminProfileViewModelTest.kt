package com.example.supporthub.features.admin

import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `load profile maps realtime admin snapshot into polished ui state`() = runTest(dispatcher) {
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakeAdminProfileRepository(initialState = sampleProfile())
            val viewModel = AdminProfileViewModel(repository)

            viewModel.loadProfile()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Maya Patel", state.fullName)
            assertEquals("maya.patel@supporthub.io", state.email)
            assertEquals("Austin, TX", state.location)
            assertEquals("Head of Support", state.jobTitle)
            assertEquals("Customer Operations", state.department)
            assertEquals("SupportHub North America", state.workspaceName)
            assertEquals("MP", state.avatarInitials)
            assertEquals("Admin", state.roleLabel)
            assertEquals("Active", state.statusLabel)
            assertEquals("Online", state.statusBadgeLabel)
            assertEquals("Email", state.authProviderLabel)
            assertEquals("18", state.resolvedTicketsLabel)
            assertEquals("4.9", state.csatLabel)
            assertEquals("2m", state.responseTimeLabel)
            assertNull(state.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `logout exposes completion event after repository succeeds`() = runTest(dispatcher) {
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakeAdminProfileRepository(initialState = sampleProfile())
            val viewModel = AdminProfileViewModel(repository)

            viewModel.logout()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isLoggedOut)
            assertFalse(state.isLoggingOut)
            assertEquals(1, repository.logoutCalls)
            assertNull(state.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun sampleProfile(): AdminProfileData {
        val user = User(
            uid = "admin-1",
            fullName = "Maya Patel",
            email = "maya.patel@supporthub.io",
            phone = "+1 (512) 555-0198",
            location = "Austin, TX",
            jobTitle = "Head of Support",
            department = "Customer Operations",
            workspaceName = "SupportHub North America",
            requestedRole = Role.ADMIN.value,
            approvedRole = Role.ADMIN.value,
            status = "active",
            authProvider = "email"
        )

        return AdminProfileData(
            user = user,
            summary = AdminProfileSummary(
                totalUsers = 18,
                totalTickets = 42,
                pendingApprovals = 6,
                activeAgents = 11
            )
        )
    }
}

private class FakeAdminProfileRepository(
    initialState: AdminProfileData,
    private val logoutError: Throwable? = null
) : AdminProfileRepository {

    private val profileState = MutableStateFlow(initialState)

    var logoutCalls: Int = 0

    fun emit(state: AdminProfileData) {
        profileState.value = state
    }

    override fun observeProfile(): Flow<AdminProfileData?> = profileState

    override suspend fun updateProfile(update: AdminProfileUpdate) {
        // no-op for view model tests
    }

    override suspend fun logout() {
        logoutCalls += 1
        logoutError?.let { throw it }
    }
}
