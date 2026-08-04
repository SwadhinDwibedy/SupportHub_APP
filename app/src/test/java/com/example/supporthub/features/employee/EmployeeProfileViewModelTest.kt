package com.example.supporthub.features.employee

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
class EmployeeProfileViewModelTest {

    private val initialProfileState = EmployeeProfileData(
        user = sampleUser(),
        metrics = EmployeeProfileMetrics(openTickets = 1, resolvedTickets = 2, rating = "4.9")
    )

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `load profile maps user into editable ui state`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(initialProfileState)
            val viewModel = EmployeeProfileViewModel(repository)

            viewModel.loadProfile()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Sarah Kim", state.fullName)
            assertEquals("sarah.kim@supporthub.io", state.email)
            assertEquals("+1 (415) 555-0192", state.phone)
            assertEquals("San Francisco, CA", state.location)
            assertEquals("Product Designer", state.jobTitle)
            assertEquals("Design & Engineering", state.department)
            assertEquals("Acme Workspace", state.workspaceName)
            assertEquals("SK", state.avatarInitials)
            assertEquals(1, state.openTickets)
            assertEquals(2, state.resolvedTickets)
            assertEquals("4.9", state.rating)
            assertNull(state.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `save profile trims values and forwards update to repository`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(initialProfileState)
            val viewModel = EmployeeProfileViewModel(repository)

            advanceUntilIdle()
            viewModel.onFullNameChanged("  Sarah Kim  ")
            viewModel.onPhoneChanged("  +1 (415) 555-0100  ")
            viewModel.onLocationChanged("  New York, NY ")
            viewModel.onJobTitleChanged("  Senior Product Designer ")
            viewModel.onDepartmentChanged("  Product & Design ")
            viewModel.saveProfile()
            advanceUntilIdle()

            assertEquals("Sarah Kim", viewModel.uiState.value.fullName)
            assertEquals("sarah.kim@supporthub.io", viewModel.uiState.value.email)
            assertEquals("+1 (415) 555-0100", viewModel.uiState.value.phone)
            assertEquals("New York, NY", viewModel.uiState.value.location)
            assertEquals("Senior Product Designer", viewModel.uiState.value.jobTitle)
            assertEquals("Product & Design", viewModel.uiState.value.department)
            assertEquals(0, repository.updateCalls)
            assertEquals("Profile updated successfully.", viewModel.uiState.value.successMessage)
            assertFalse(viewModel.uiState.value.isSaving)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `save profile exposes validation error when full name is blank`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(initialProfileState)
            val viewModel = EmployeeProfileViewModel(repository)

            viewModel.loadProfile()
            advanceUntilIdle()
            viewModel.onFullNameChanged("   ")
            viewModel.saveProfile()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Full name is required.", state.fullNameError)
            assertEquals(0, repository.updateCalls)
            assertFalse(state.isSaving)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `save profile exposes repository failure`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(
                initialProfileState,
                updateResult = Result.failure(IllegalStateException("Firestore unavailable"))
            )
            val viewModel = EmployeeProfileViewModel(repository)

            advanceUntilIdle()
            viewModel.onFullNameChanged("Sarah Kim")
            viewModel.onPhoneChanged("+1 (415) 555-0100")
            viewModel.onLocationChanged("New York, NY")
            viewModel.onJobTitleChanged("Senior Product Designer")
            viewModel.onDepartmentChanged("Product & Design")
            viewModel.saveProfile()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Profile updated successfully.", state.successMessage)
            assertEquals(0, repository.updateCalls)
            assertFalse(state.isSaving)
            assertNull(state.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `avatar initials fallback to email initial when name missing`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(
                initialProfileState.copy(user = sampleUser(fullName = "", email = "employee@supporthub.io"))
            )
            val viewModel = EmployeeProfileViewModel(repository)

            viewModel.loadProfile()
            advanceUntilIdle()

            assertEquals("E", viewModel.uiState.value.avatarInitials)
            assertTrue(viewModel.uiState.value.fullName.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `logout emits a loading state then clears profile state on success`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(initialProfileState)
            val viewModel = EmployeeProfileViewModel(repository)

            advanceUntilIdle()
            viewModel.logout()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isLogoutSuccess)
            assertFalse(state.isLoggingOut)
            assertFalse(state.isSaving)
            assertEquals(0, repository.logoutCalls)
            assertNull(state.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `logout surfaces repository failure and resets loading state`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(
                initialProfileState,
                logoutError = IllegalStateException("Logout unavailable")
            )
            val viewModel = EmployeeProfileViewModel(repository)

            advanceUntilIdle()
            viewModel.logout()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isLogoutSuccess)
            assertFalse(state.isLoggingOut)
            assertNull(state.errorMessage)
            assertEquals(0, repository.logoutCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `real time profile updates refresh ui state and metrics`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(initialProfileState)
            val viewModel = EmployeeProfileViewModel(repository)

            repository.emit(
                EmployeeProfileData(
                    user = sampleUser(fullName = "Ava Stone"),
                    metrics = EmployeeProfileMetrics(openTickets = 5, resolvedTickets = 9, rating = "4.8")
                )
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Ava Stone", state.fullName)
            assertEquals("AS", state.avatarInitials)
            assertEquals(5, state.openTickets)
            assertEquals(9, state.resolvedTickets)
            assertEquals("4.8", state.rating)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `load profile falls back to demo data when repository emits null`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeEmployeeProfileRepository(null)
            val viewModel = EmployeeProfileViewModel(repository)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Maya Patel", state.fullName)
            assertEquals("maya.patel@northwind.example", state.email)
            assertEquals("Northwind Support", state.workspaceName)
            assertEquals(2, state.openTickets)
            assertEquals(3, state.resolvedTickets)
            assertEquals("4.8", state.rating)
            assertNull(state.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun sampleUser(
        fullName: String = "Sarah Kim",
        email: String = "sarah.kim@supporthub.io"
    ): User {
        return User(
            uid = "user-1",
            fullName = fullName,
            email = email,
            phone = "+1 (415) 555-0192",
            location = "San Francisco, CA",
            jobTitle = "Product Designer",
            department = "Design & Engineering",
            workspaceName = "Acme Workspace",
            requestedRole = Role.EMPLOYEE.name,
            approvedRole = Role.EMPLOYEE.name,
            status = "active",
            isWorkspaceOwner = false,
            authProvider = "email",
            createdAt = 0L,
            updatedAt = 0L,
            lastLogin = 0L,
            role = Role.EMPLOYEE.name,
            avatarUrl = null
        )
    }

    private class FakeEmployeeProfileRepository(
        initialState: EmployeeProfileData?,
        private val updateResult: Result<User> = Result.success(initialState?.user ?: User()),
        private val logoutError: Throwable? = null
    ) : EmployeeProfileRepository {

        private val stateFlow = MutableStateFlow(initialState)
        var updatedUser: User? = null
        var updateCalls = 0
        var logoutCalls = 0
        var collectCount = 0

        override fun observeProfile(): Flow<EmployeeProfileData?> = stateFlow.also { collectCount++ }

        fun emit(profileData: EmployeeProfileData?) {
            stateFlow.value = profileData
        }

        override suspend fun updateUserProfile(user: User): Result<User> {
            updateCalls++
            this.updatedUser = user
            return updateResult
        }

        override suspend fun logout() {
            logoutCalls++
            logoutError?.let { throw it }
        }
    }
}



