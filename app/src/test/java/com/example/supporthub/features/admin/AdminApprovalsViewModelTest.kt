package com.example.supporthub.features.admin

import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.model.Workspace
import com.example.supporthub.features.authentication.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminApprovalsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `loads workspace users and separates pending approvals from active users`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeAdminUsersRepository()
            val viewModel = AdminApprovalsViewModel(repository)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(2, state.workspaceUsers.size)
            assertEquals(1, state.pendingUsers.size)
            assertEquals("Pending User", state.pendingUsers.first().fullName)
            assertEquals("Active User", state.workspaceUsers.first { it.status == "active" }.fullName)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeAdminUsersRepository : AuthRepository {
    override suspend fun resolveStartupUser(): User? = null

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

    override suspend fun getCurrentUser(): User? = User(
        uid = "admin-1",
        fullName = "Admin User",
        email = "admin@example.com",
        workspaceName = "Acme"
    )

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
        Result.success(emptyList())

    override suspend fun updateUserProfile(user: User): Result<User> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun approveUser(uid: String, approvedRole: Role): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun rejectUser(uid: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>> =
        Result.success(
            listOf(
                sampleUser(uid = "active-1", fullName = "Active User", status = "active", workspaceName = workspaceName),
                sampleUser(uid = "pending-1", fullName = "Pending User", status = "pending", workspaceName = workspaceName)
            )
        )

    private fun sampleUser(
        uid: String,
        fullName: String,
        status: String,
        workspaceName: String = "Acme"
    ): User = User(
        uid = uid,
        fullName = fullName,
        email = "$uid@example.com",
        department = "Support",
        workspaceName = workspaceName,
        status = status,
        requestedRole = Role.EMPLOYEE.value
    )
}
