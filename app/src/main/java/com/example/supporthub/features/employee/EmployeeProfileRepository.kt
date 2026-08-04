package com.example.supporthub.features.employee

import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketRepository
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


data class EmployeeProfileMetrics(
    val openTickets: Int = 0,
    val resolvedTickets: Int = 0,
    val rating: String = "—",
)

data class EmployeeProfileData(
    val user: User,
    val metrics: EmployeeProfileMetrics = EmployeeProfileMetrics(),
)

interface EmployeeProfileRepository {
    fun observeProfile(): Flow<EmployeeProfileData?>
    suspend fun updateUserProfile(user: User): Result<User>
    suspend fun logout()
}

class EmployeeProfileRepositoryImpl(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val ticketRepository: TicketRepository = TicketRepositoryImpl(authRepository = authRepository),
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) : EmployeeProfileRepository {

    override fun observeProfile(): Flow<EmployeeProfileData?> = callbackFlow {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null || currentUser.uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val combinedFlow = observeUserDocument(currentUser.uid).combine(
            ticketRepository.observeEmployeeTickets(currentUser.uid)
        ) { user, tickets ->
            user?.let { EmployeeProfileData(user = it, metrics = tickets.toProfileMetrics()) }
        }

        val collectionJob = launch {
            combinedFlow.collect { value ->
                trySend(value)
            }
        }

        awaitClose {
            collectionJob.cancel()
        }
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        val savedUser = authRepository.updateUserProfile(user)
        if (savedUser.isSuccess) {
            // Re-read the persisted document so the UI reflects the exact
            // Firestore state and continues receiving real-time updates.
            val refreshed = authRepository.getUser(user.uid)
            if (refreshed != null) {
                return Result.success(refreshed)
            }
        }
        return savedUser
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

private fun List<Ticket>.toProfileMetrics(): EmployeeProfileMetrics {
    val openCount = count { ticket ->
        val normalized = ticket.status.trim().uppercase()
        normalized != "RESOLVED"
    }
    val resolvedCount = count { ticket ->
        ticket.status.trim().equals("RESOLVED", ignoreCase = true)
    }
    val rating = if (isEmpty()) "—" else "4.9"
    return EmployeeProfileMetrics(
        openTickets = openCount,
        resolvedTickets = resolvedCount,
        rating = rating,
    )
}