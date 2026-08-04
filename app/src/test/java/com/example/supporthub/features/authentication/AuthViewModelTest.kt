package com.example.supporthub.features.authentication

import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.model.Workspace
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.utils.StartupDestination
import com.example.supporthub.features.authentication.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `logout exposes login as a fresh startup navigation target`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeAuthRepository()
            val viewModel = AuthViewModel(repository)
            advanceUntilIdle()

            viewModel.logout()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(StartupDestination.Login, state.startupDestination)
            assertEquals(StartupDestination.Login, state.startupFlow.navigationTarget)
            assertNull(state.user)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `startup resolution uses approved employee role and ignores requested role`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeAuthRepository(
                startupUser = User(
                    uid = "uid",
                    fullName = "Employee User",
                    email = "employee@example.com",
                    workspaceName = "Acme",
                    requestedRole = "admin",
                    approvedRole = "employee",
                    status = "active"
                )
            )
            val viewModel = AuthViewModel(repository)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(StartupDestination.EmployeeDashboard, state.startupDestination)
            assertEquals(StartupDestination.EmployeeDashboard, state.startupFlow.navigationTarget)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `startup resolution routes approved admin role to admin dashboard`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeAuthRepository(
                startupUser = User(
                    uid = "uid",
                    fullName = "Admin User",
                    email = "admin@example.com",
                    workspaceName = "Acme",
                    requestedRole = "employee",
                    approvedRole = "admin",
                    status = "active"
                )
            )
            val viewModel = AuthViewModel(repository)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(StartupDestination.AdminDashboard, state.startupDestination)
            assertEquals(StartupDestination.AdminDashboard, state.startupFlow.navigationTarget)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeAuthRepository(
    private val startupUser: User? = null,
    private val currentUser: User? = null
) : AuthRepository {

    override suspend fun resolveStartupUser(): User? = startupUser

    override suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        workspaceName: String,
        role: Role
    ): Result<User> = Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun loginUser(
        email: String,
        password: String
    ): Result<User> = Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun signInWithGoogle(idToken: String): Result<User> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun logout() = Unit

    override suspend fun getCurrentUser(): User? = currentUser

    override suspend fun checkWorkspaceExists(workspaceName: String): Boolean = false

    override suspend fun createWorkspace(workspaceName: String, ownerUid: String): String =
        throw UnsupportedOperationException("Not used in test")

    override suspend fun saveUser(user: User) = Unit

    override suspend fun getUser(uid: String): User? = null

    override suspend fun getWorkspaceByName(workspaceName: String): Workspace? = null

    override suspend fun completeGoogleProfile(
        fullName: String,
        email: String,
        workspaceName: String,
        role: Role
    ): Result<User> = Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun getPendingUsers(): Result<List<User>> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun updateUserProfile(user: User): Result<User> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun approveUser(uid: String, approvedRole: Role): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun rejectUser(uid: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not used in test"))
}
