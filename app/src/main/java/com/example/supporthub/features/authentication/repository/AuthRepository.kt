package com.example.supporthub.features.authentication.repository

import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.model.Workspace

interface AuthRepository {

    suspend fun resolveStartupUser(): User?

    suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        workspaceName: String,
        role: Role
    ): Result<User>

    suspend fun loginUser(
        email: String,
        password: String
    ): Result<User>

    suspend fun signInWithGoogle(
        idToken: String
    ): Result<User>

    suspend fun sendPasswordResetEmail(
        email: String
    ): Result<Unit>

    suspend fun logout()

    suspend fun getCurrentUser(): User?

    suspend fun checkWorkspaceExists(
        workspaceName: String
    ): Boolean

    suspend fun createWorkspace(
        workspaceName: String,
        ownerUid: String
    ): String

    suspend fun saveUser(
        user: User
    )

    suspend fun getUser(
        uid: String
    ): User?

    suspend fun getWorkspaceByName(
        workspaceName: String
    ): Workspace?

    suspend fun completeGoogleProfile(
        fullName: String,
        email: String,
        workspaceName: String,
        role: Role
    ): Result<User>

    suspend fun getPendingUsers(): Result<List<User>>

    suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>>

    suspend fun updateUserProfile(user: User): Result<User>

    suspend fun approveUser(
        uid: String,
        approvedRole: Role
    ): Result<Unit>

    suspend fun rejectUser(uid: String): Result<Unit>
}
