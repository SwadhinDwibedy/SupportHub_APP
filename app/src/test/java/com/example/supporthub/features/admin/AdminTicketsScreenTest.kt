package com.example.supporthub.features.admin

import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.tickets.TICKET_STATUS_OPEN
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketAssignmentUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AdminTicketsScreenTest {

    private val zoneId = ZoneId.of("Asia/Kolkata")
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `build admin tickets ui state counts workspace tickets and filters by selected tab`() {
        val tickets = listOf(
            sampleTicket(ticketId = "4092", subject = "VPN access not working", status = TICKET_STATUS_OPEN, createdAt = nowMinusMinutes(10)),
            sampleTicket(ticketId = "4091", subject = "Salesforce sync timeout", status = "PENDING", createdAt = nowMinusMinutes(9)),
            sampleTicket(ticketId = "4089", subject = "Request new monitor", status = "IN_PROGRESS", createdAt = nowMinusMinutes(8)),
            sampleTicket(ticketId = "4088", subject = "Printer needs replacement", status = "RESOLVED", createdAt = nowMinusMinutes(7)),
            sampleTicket(ticketId = "4087", subject = "Other workspace issue", workspaceName = "Other", status = TICKET_STATUS_OPEN, createdAt = nowMinusMinutes(6)),
        )

        val state = buildAdminTicketsUiState(
            tickets = tickets,
            selectedFilter = AdminTicketStatusFilter.PENDING,
            searchQuery = "salesforce",
            workspaceName = "Acme",
        )

        assertEquals(4, state.totalTicketsCount)
        assertEquals(1, state.openTicketsCount)
        assertEquals(2, state.pendingTicketsCount)
        assertEquals(1, state.resolvedTicketsCount)
        assertEquals(listOf("#4091"), state.visibleTickets.map { it.ticketId })
    }

    @Test
    fun `visible admin ticket cards expose assigned and unassigned assignment chips`() {
        val state = buildAdminTicketsUiState(
            tickets = listOf(
                sampleTicket(
                    ticketId = "4092",
                    subject = "VPN access not working",
                    status = TICKET_STATUS_OPEN,
                    createdAt = nowMinusMinutes(10),
                    assignedAgentId = null,
                    assignedAgentName = null,
                    assignedAgentEmail = null,
                    assignedTimestamp = null,
                    assignedByAdmin = null,
                    assignmentStatus = null,
                ),
                sampleTicket(
                    ticketId = "4091",
                    subject = "Salesforce sync timeout",
                    status = "IN_PROGRESS",
                    createdAt = nowMinusMinutes(9),
                    assignedAgentId = "agent-7",
                    assignedAgentName = "John Doe",
                    assignedAgentEmail = "john@company.com",
                    assignedAgentDepartment = "IT Support",
                    assignedTimestamp = nowMinusMinutes(2),
                    assignedByAdmin = "admin-1",
                    assignmentStatus = "Assigned",
                )
            ),
            selectedFilter = AdminTicketStatusFilter.ALL,
            searchQuery = "",
            workspaceName = "Acme",
        )

        val unassigned = state.visibleTickets.first { it.ticketId == "#4092" }
        val assigned = state.visibleTickets.first { it.ticketId == "#4091" }

        assertEquals("Unassigned", unassigned.assignmentStatusLabel)
        assertEquals("Assigned", assigned.assignmentStatusLabel)
        assertEquals("John Doe", assigned.assignmentAgentName)
        assertEquals("IT Support", assigned.assignmentAgentDepartment)
    }

    @Test
    fun `support agents are sorted by lowest active tickets first`() {
        val agents = listOf(
            sampleAgent(uid = "agent-3", fullName = "John Doe", department = "IT Support", activeTickets = 7, isOnline = true),
            sampleAgent(uid = "agent-1", fullName = "Sarah", department = "IT Support", activeTickets = 2, isOnline = true),
            sampleAgent(uid = "agent-4", fullName = "Alex", department = "Customer Success", activeTickets = 11, isOnline = false),
            sampleAgent(uid = "agent-2", fullName = "Mike", department = "Network", activeTickets = 4, isOnline = true),
        )

        val sorted = agents.sortedWith(
            compareBy<SupportAgentOption> { it.activeTicketCount }
                .thenByDescending { it.isOnline }
                .thenBy { it.fullName.lowercase() }
        )

        assertEquals(listOf("Sarah", "Mike", "John Doe", "Alex"), sorted.map { it.fullName })
        assertEquals(listOf(2, 4, 7, 11), sorted.map { it.activeTicketCount })
    }

    @Test
    fun `view model reacts to realtime repository updates and workspace filtering`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val ticketsFlow = MutableStateFlow(
                listOf(
                    sampleTicket(ticketId = "4092", subject = "VPN access not working", status = TICKET_STATUS_OPEN, createdAt = nowMinusMinutes(10)),
                    sampleTicket(ticketId = "4091", subject = "Salesforce sync timeout", status = "PENDING", createdAt = nowMinusMinutes(9)),
                    sampleTicket(ticketId = "4087", subject = "Other workspace issue", workspaceName = "Other", status = "RESOLVED", createdAt = nowMinusMinutes(6)),
                )
            )
            val repository = FakeAdminTicketsRepository(
                currentUser = sampleUser(),
                ticketsFlow = ticketsFlow
            )
            val viewModel = AdminTicketsViewModel(repository)

            advanceUntilIdle()

            val initialState = viewModel.uiState.value
            assertEquals(2, initialState.totalTicketsCount)
            assertEquals(1, initialState.openTicketsCount)
            assertEquals(1, initialState.pendingTicketsCount)
            assertEquals(0, initialState.resolvedTicketsCount)
            assertEquals(listOf("#4091", "#4092"), initialState.visibleTickets.map { it.ticketId })

            ticketsFlow.value = listOf(
                sampleTicket(ticketId = "4092", subject = "VPN access not working", status = TICKET_STATUS_OPEN, createdAt = nowMinusMinutes(10)),
                sampleTicket(ticketId = "4091", subject = "Salesforce sync timeout", status = "PENDING", createdAt = nowMinusMinutes(9)),
                sampleTicket(ticketId = "4089", subject = "Request new monitor", status = "RESOLVED", createdAt = nowMinusMinutes(5)),
            )

            advanceUntilIdle()

            val updatedState = viewModel.uiState.value
            assertEquals(3, updatedState.totalTicketsCount)
            assertEquals(1, updatedState.resolvedTicketsCount)
            assertFalse(updatedState.isLoading)
            assertEquals(listOf("#4089", "#4091", "#4092"), updatedState.visibleTickets.map { it.ticketId })
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun sampleUser(): User = User(
        uid = "admin-1",
        fullName = "Admin User",
        email = "admin@example.com",
        workspaceName = "Acme",
        status = "active",
        requestedRole = "admin",
        approvedRole = "admin"
    )

    private fun sampleTicket(
        ticketId: String,
        subject: String,
        workspaceName: String = "Acme",
        status: String,
        createdAt: Any,
        assignedAgentId: String? = null,
        assignedAgentName: String? = null,
        assignedAgentEmail: String? = null,
        assignedAgentDepartment: String? = null,
        assignedTimestamp: Any? = null,
        assignedByAdmin: String? = null,
        assignmentStatus: String? = null,
    ): Ticket = Ticket(
        ticketId = ticketId,
        employeeUid = "employee-$ticketId",
        employeeName = "Requester $ticketId",
        employeeEmail = "requester$ticketId@example.com",
        workspaceName = workspaceName,
        subject = subject,
        description = "Description",
        category = "IT",
        priority = "High",
        status = status,
        assignedAgentId = assignedAgentId,
        assignedAgentName = assignedAgentName,
        assignedAgentEmail = assignedAgentEmail,
        assignedAgentDepartment = assignedAgentDepartment,
        assignedTimestamp = assignedTimestamp,
        assignedByAdmin = assignedByAdmin,
        assignmentStatus = assignmentStatus,
        createdAt = createdAt,
    )

    private fun sampleAgent(
        uid: String,
        fullName: String,
        department: String,
        activeTickets: Int,
        isOnline: Boolean,
    ) = SupportAgentOption(
        uid = uid,
        fullName = fullName,
        department = department,
        activeTicketCount = activeTickets,
        isOnline = isOnline,
        initials = fullName.take(2).uppercase(),
        email = "$uid@company.com",
    )

    private fun nowMinusMinutes(minutes: Long) = ZonedDateTime.of(2026, 7, 21, 18, 0, 0, 0, zoneId).minusMinutes(minutes).toInstant()

    private class FakeAdminTicketsRepository(
        private val currentUser: User?,
        private val ticketsFlow: MutableStateFlow<List<Ticket>>,
    ) : AdminTicketsRepository {
        override suspend fun getCurrentUser(): User? = currentUser
        override fun observeAllTickets() = ticketsFlow
        override suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>> =
            Result.success(emptyList())
        override suspend fun assignTicket(ticketId: String, assignment: TicketAssignmentUpdate): Result<Unit> =
            Result.success(Unit)
    }
}
