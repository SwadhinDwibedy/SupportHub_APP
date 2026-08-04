package com.example.supporthub.features.admin

import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


data class AdminProfileSummary(
    val totalUsers: Int = 0,
    val totalTickets: Int = 0,
    val pendingApprovals: Int = 0,
    val activeAgents: Int = 0,
)

data class AdminProfileData(
    val user: User,
    val summary: AdminProfileSummary = AdminProfileSummary(),
)

interface AdminProfileRepository {
    fun observeProfile(): Flow<AdminProfileData?>
    suspend fun updateProfile(update: AdminProfileUpdate)
    suspend fun logout()
}

data class AdminProfileUpdate(
    val fullName: String,
    val phone: String,
    val department: String,
    val jobTitle: String,
    val location: String,
    val avatarUrl: String? = null,
)

class FirebaseAdminProfileRepository(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val summaryRepository: AdminSummaryRepository = FirebaseAdminSummaryRepository(),
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) : AdminProfileRepository {

    override fun observeProfile(): Flow<AdminProfileData?> = channelFlow {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null || currentUser.uid.isBlank()) {
            send(null)
            return@channelFlow
        }

        val workspaceName = currentUser.workspaceName.trim()
        if (workspaceName.isBlank()) {
            send(null)
            return@channelFlow
        }

        val combinedFlow = observeUserDocument(currentUser.uid).combine(
            summaryRepository.observeWorkspaceSummary()
        ) { user, workspaceSummary ->
            user?.let {
                AdminProfileData(
                    user = it,
                    summary = workspaceSummary.toAdminProfileSummary()
                )
            }
        }

        launch {
            combinedFlow.collect { send(it) }
        }
    }

    override suspend fun updateProfile(update: AdminProfileUpdate) {
        val currentUser = authRepository.getCurrentUser() ?: return
        if (currentUser.uid.isBlank()) return

        val updates = mutableMapOf<String, Any>(
            "fullName" to update.fullName,
            "phone" to update.phone,
            "department" to update.department,
            "jobTitle" to update.jobTitle,
            "location" to update.location,
        )
        update.avatarUrl?.takeIf { it.isNotBlank() }?.let { updates["avatarUrl"] = it }

        firestore.collection(FirebaseCollections.USERS)
            .document(currentUser.uid)
            .update(updates)
            .await()
    }

    override suspend fun logout() {
        authRepository.logout()
    }

    private fun observeUserDocument(uid: String): Flow<User?> {
        if (uid.isBlank()) {
            return flowOf(null)
        }

        return callbackFlow {
            val registration = firestore
                .collection(FirebaseCollections.USERS)
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    trySend(snapshot?.toObject(User::class.java))
                }

            awaitClose { registration.remove() }
        }
    }
}

private fun AdminWorkspaceSummary.toAdminProfileSummary(): AdminProfileSummary {
    val activeAgents = users.count { user ->
        user.status.equals("active", ignoreCase = true) &&
            user.approvedRole.equals("agent", ignoreCase = true)
    }

    return AdminProfileSummary(
        totalUsers = users.size,
        totalTickets = tickets.size,
        pendingApprovals = users.count { user -> user.status.equals("pending", ignoreCase = true) },
        activeAgents = activeAgents,
    )
}
