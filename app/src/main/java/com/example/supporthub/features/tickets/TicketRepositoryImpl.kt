package com.example.supporthub.features.tickets

import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TicketRepositoryImpl(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val firestoreDataSource: TicketFirestoreDataSource = TicketFirestoreDataSource(),
) : TicketRepository {

    override suspend fun getCurrentUserProfile(): User? = authRepository.getCurrentUser()

    override suspend fun createTicket(draft: TicketDraft): Result<Unit> {
        val user = getCurrentUserProfile()
            ?: return Result.failure(IllegalStateException("Unable to find logged-in user profile."))

        val payload = draft.toSubmissionPayload(user)

        if (payload.employeeUid.isBlank()) {
            return Result.failure(IllegalStateException("Employee profile is missing uid."))
        }
        if (payload.employeeEmail.isBlank()) {
            return Result.failure(IllegalStateException("Employee profile is missing email."))
        }
        if (payload.workspaceName.isBlank()) {
            return Result.failure(IllegalStateException("Employee profile is missing workspace name."))
        }

        return firestoreDataSource.createTicket(payload)
    }

    override fun observeEmployeeTickets(employeeUid: String): Flow<List<Ticket>> {
        if (employeeUid.isBlank()) {
            return flowOf(emptyList())
        }
        return firestoreDataSource.observeEmployeeTickets(employeeUid)
    }

    override fun observeAllTickets(): Flow<List<Ticket>> {
        return firestoreDataSource.observeAllTickets()
    }

    override suspend fun getTicketById(ticketId: String): Result<Ticket> {
        if (ticketId.isBlank()) {
            return Result.failure(IllegalStateException("Ticket id is missing."))
        }
        return firestoreDataSource.getTicketById(ticketId)
    }

    override suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>> {
        return authRepository.getWorkspaceUsers(workspaceName)
    }

    override suspend fun assignTicket(ticketId: String, assignment: TicketAssignmentUpdate): Result<Unit> {
        if (ticketId.isBlank()) {
            return Result.failure(IllegalStateException("Ticket id is missing."))
        }
        return firestoreDataSource.updateTicketAssignment(ticketId, assignment)
    }

    override suspend fun updateTicketDetails(ticketId: String, updates: Map<String, Any?>): Result<Unit> {
        if (ticketId.isBlank()) {
            return Result.failure(IllegalStateException("Ticket id is missing."))
        }
        return firestoreDataSource.updateTicketDetails(ticketId, updates)
    }
}
