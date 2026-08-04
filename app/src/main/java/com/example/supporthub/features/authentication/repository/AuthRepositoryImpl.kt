package com.example.supporthub.features.authentication.repository

import com.example.supporthub.core.firebase.FirebaseAuthManager
import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.model.Workspace
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuthManager.auth
    private val firestore = FirestoreManager.firestore

    override suspend fun resolveStartupUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return getUser(firebaseUser.uid)
    }

    override suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        workspaceName: String,
        role: Role
    ): Result<User> {

        return try {

            val authResult = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("User creation failed"))

            val user = buildPendingUser(
                uid = firebaseUser.uid,
                fullName = fullName,
                email = email,
                workspaceName = workspaceName,
                role = role,
                authProvider = "email"
            )

            ensureWorkspaceExists(
                workspaceName = workspaceName,
                ownerUid = firebaseUser.uid
            )
            saveUser(user)

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    override suspend fun loginUser(
        email: String,
        password: String
    ): Result<User> {

        return try {

            auth.signInWithEmailAndPassword(
                email,
                password
            ).await()

            val firebaseUser = auth.currentUser
                ?: return Result.failure(
                    Exception("Login failed.")
                )

            val user = getUser(firebaseUser.uid)
                ?: return Result.failure(
                    Exception("User data not found.")
                )

            firestore
                .collection(FirebaseCollections.USERS)
                .document(firebaseUser.uid)
                .update(
                    "lastLogin",
                    System.currentTimeMillis()
                )
                .await()

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    override suspend fun signInWithGoogle(
        idToken: String
    ): Result<User> {

        return try {

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            val authResult = auth
                .signInWithCredential(credential)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(
                    Exception("Google sign in failed.")
                )

            val existingUser = getUser(firebaseUser.uid)

            if (existingUser != null) {

                firestore
                    .collection(FirebaseCollections.USERS)
                    .document(firebaseUser.uid)
                    .update(
                        "lastLogin",
                        System.currentTimeMillis()
                    )
                    .await()

                return Result.success(existingUser)
            }

            Result.success(
                User(
                    uid = firebaseUser.uid,
                    fullName = firebaseUser.displayName.orEmpty(),
                    email = firebaseUser.email.orEmpty(),
                    authProvider = "google",
                    status = ""
                )
            )

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    override suspend fun sendPasswordResetEmail(
        email: String
    ): Result<Unit> {

        return try {

            auth
                .sendPasswordResetEmail(email)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUser(): User? {

        val firebaseUser = auth.currentUser
            ?: return null

        return getUser(firebaseUser.uid)
    }

    override suspend fun checkWorkspaceExists(
        workspaceName: String
    ): Boolean {

        val normalizedWorkspaceKey = WorkspaceRegistrationPolicy
            .normalizeWorkspaceKey(workspaceName)

        return firestore
            .collection(FirebaseCollections.WORKSPACES)
            .document(normalizedWorkspaceKey)
            .get()
            .await()
            .exists()
    }

    override suspend fun createWorkspace(
        workspaceName: String,
        ownerUid: String
    ): String {

        val normalizedWorkspaceKey = WorkspaceRegistrationPolicy
            .normalizeWorkspaceKey(workspaceName)

        val workspace = Workspace(
            workspaceName = WorkspaceRegistrationPolicy.normalizeWorkspaceDisplayName(workspaceName),
            normalizedWorkspaceKey = normalizedWorkspaceKey,
            ownerUid = ownerUid
        )

        firestore
            .collection(FirebaseCollections.WORKSPACES)
            .document(normalizedWorkspaceKey)
            .set(workspace)
            .await()

        return normalizedWorkspaceKey
    }

    override suspend fun saveUser(
        user: User
    ) {

        firestore
            .collection(FirebaseCollections.USERS)
            .document(user.uid)
            .set(user, SetOptions.merge())
            .await()

    }

    override suspend fun getUser(
        uid: String
    ): User? {

        val snapshot = firestore
            .collection(FirebaseCollections.USERS)
            .document(uid)
            .get()
            .await()

        return snapshot.toObject(User::class.java)
    }

    override suspend fun getWorkspaceByName(
        workspaceName: String
    ): Workspace? {

        val normalizedWorkspaceKey = WorkspaceRegistrationPolicy
            .normalizeWorkspaceKey(workspaceName)

        val snapshot = firestore
            .collection(FirebaseCollections.WORKSPACES)
            .document(normalizedWorkspaceKey)
            .get()
            .await()

        return snapshot.toObject(Workspace::class.java)
    }

    override suspend fun completeGoogleProfile(
        fullName: String,
        email: String,
        workspaceName: String,
        role: Role
    ): Result<User> {

        return try {

            val firebaseUser = auth.currentUser
                ?: return Result.failure(Exception("User not found"))

            val user = buildPendingUser(
                uid = firebaseUser.uid,
                fullName = fullName,
                email = email,
                workspaceName = workspaceName,
                role = role,
                authProvider = "google"
            )

            ensureWorkspaceExists(
                workspaceName = workspaceName,
                ownerUid = firebaseUser.uid
            )
            saveUser(user)

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)

        }

    }

    override suspend fun getPendingUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore
                .collection(FirebaseCollections.USERS)
                .whereEqualTo("status", "pending")
                .get()
                .await()
 
            val users = snapshot.documents
                .mapNotNull { it.toObject(User::class.java) }
                .sortedBy { it.createdAt }
 
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>> {
        return try {
            val normalizedWorkspaceName = workspaceName.trim()
            if (normalizedWorkspaceName.isBlank()) {
                return Result.success(emptyList())
            }

            val snapshot = firestore
                .collection(FirebaseCollections.USERS)
                .whereEqualTo("workspaceName", normalizedWorkspaceName)
                .get()
                .await()

            val users = snapshot.documents
                .mapNotNull { it.toObject(User::class.java) }
                .sortedBy { it.fullName.lowercase() }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
            firestore
                .collection(FirebaseCollections.USERS)
                .document(updatedUser.uid)
                .set(
                    mapOf(
                        "uid" to updatedUser.uid,
                        "fullName" to updatedUser.fullName,
                        "email" to updatedUser.email,
                        "phone" to updatedUser.phone,
                        "location" to updatedUser.location,
                        "jobTitle" to updatedUser.jobTitle,
                        "department" to updatedUser.department,
                        "workspaceName" to updatedUser.workspaceName,
                        "requestedRole" to updatedUser.requestedRole,
                        "approvedRole" to updatedUser.approvedRole,
                        "status" to updatedUser.status,
                        "isWorkspaceOwner" to updatedUser.isWorkspaceOwner,
                        "authProvider" to updatedUser.authProvider,
                        "createdAt" to updatedUser.createdAt,
                        "updatedAt" to updatedUser.updatedAt,
                        "lastLogin" to updatedUser.lastLogin,
                        "role" to updatedUser.role,
                        "avatarUrl" to updatedUser.avatarUrl
                    ),
                    SetOptions.merge()
                )
                .await()

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
 
    override suspend fun approveUser(
        uid: String,
        approvedRole: Role
    ): Result<Unit> {
        return try {
            firestore
                .collection(FirebaseCollections.USERS)
                .document(uid)
                .update(
                    mapOf(
                        "approvedRole" to approvedRole.value,
                        "status" to "active",
                        "isWorkspaceOwner" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectUser(uid: String): Result<Unit> {
        return try {
            firestore
                .collection(FirebaseCollections.USERS)
                .document(uid)
                .update(
                    mapOf(
                        "approvedRole" to null,
                        "status" to "pending",
                        "isWorkspaceOwner" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureWorkspaceExists(
        workspaceName: String,
        ownerUid: String
    ) {
        val normalizedWorkspaceKey = WorkspaceRegistrationPolicy
            .normalizeWorkspaceKey(workspaceName)

        val existingWorkspace = getWorkspaceByName(workspaceName)
        WorkspaceRegistrationPolicy.logWorkspaceLookup(
            requestedWorkspaceName = workspaceName,
            normalizedWorkspaceKey = normalizedWorkspaceKey,
            existingWorkspaceFound = existingWorkspace != null
        )

        if (existingWorkspace != null && existingWorkspace.normalizedWorkspaceKey != normalizedWorkspaceKey) {
            println(
                "WorkspaceRegistration: Existing workspace document mismatch requested='${workspaceName}' normalized='${normalizedWorkspaceKey}' stored='${existingWorkspace.workspaceName}' storedKey='${existingWorkspace.normalizedWorkspaceKey}'"
            )
        }

        if (existingWorkspace == null && normalizedWorkspaceKey != workspaceName) {
            println(
                "WorkspaceRegistration: Normalized workspace key differs from requested input requested='${workspaceName}' normalized='${normalizedWorkspaceKey}'"
            )
        }

        if (existingWorkspace == null) {
            WorkspaceRegistrationPolicy.logWorkspaceCreation(
                requestedWorkspaceName = workspaceName,
                normalizedWorkspaceKey = normalizedWorkspaceKey,
                ownerUid = ownerUid
            )
            createWorkspace(
                workspaceName = workspaceName,
                ownerUid = ownerUid
            )
        }
    }

    private fun buildPendingUser(
        uid: String,
        fullName: String,
        email: String,
        workspaceName: String,
        role: Role,
        authProvider: String
    ): User {
        val normalizedWorkspaceKey = WorkspaceRegistrationPolicy
            .normalizeWorkspaceKey(workspaceName)
        val decision = WorkspaceRegistrationPolicy.resolveRegistration(
            requestedRole = role
        )

        WorkspaceRegistrationPolicy.logRegistrationDecision(
            requestedWorkspaceName = workspaceName,
            normalizedWorkspaceKey = normalizedWorkspaceKey,
            requestedRole = role,
            decision = decision
        )

        return User(
            uid = uid,
            fullName = fullName,
            email = email,
            workspaceName = WorkspaceRegistrationPolicy.normalizeWorkspaceDisplayName(workspaceName),
            requestedRole = role.value,
            approvedRole = decision.approvedRole,
            status = decision.status,
            isWorkspaceOwner = decision.isWorkspaceOwner,
            authProvider = authProvider
        )
    }
}
