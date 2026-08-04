package com.example.supporthub.features.tickets

import com.example.supporthub.features.authentication.model.User
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    suspend fun getCurrentUserProfile(): User?
    suspend fun createTicket(draft: TicketDraft): Result<Unit>
    fun observeEmployeeTickets(employeeUid: String): Flow<List<Ticket>>
    fun observeAllTickets(): Flow<List<Ticket>>
    suspend fun getTicketById(ticketId: String): Result<Ticket>
    suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>>
    suspend fun assignTicket(ticketId: String, assignment: TicketAssignmentUpdate): Result<Unit>
    suspend fun updateTicketDetails(ticketId: String, updates: Map<String, Any?>): Result<Unit>
}
