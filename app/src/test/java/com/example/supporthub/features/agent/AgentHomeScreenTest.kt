package com.example.supporthub.features.agent

import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.tickets.Ticket
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgentHomeScreenTest {

    private val testDispatcher = StandardTestDispatcher()
    private val zoneId = ZoneId.of("Asia/Kolkata")
    private val now = ZonedDateTime.of(2024, 8, 15, 9, 30, 0, 0, zoneId)

    @Test
    fun `build agent home ui state maps assigned and resolved tickets`() {
        val state = buildAgentHomeUiState(
            snapshot = AgentHomeWorkspaceSnapshot(
                currentUser = sampleAgent(),
                tickets = sampleTickets()
            ),
            now = now.toDate()
        )

        assertEquals("09:30 AM", state.header.timeLabel)
        assertEquals("Thursday, 15 August", state.header.dateLabel)
        assertEquals("Alex Rivera", state.header.agentName)
        assertEquals("Good Morning", state.header.greeting)
        assertEquals("Ready to help your customers today?", state.header.prompt)
        assertEquals("AR", state.header.avatarInitials)
        assertEquals(2, state.assignedTicketsCount)
        assertEquals(0, state.resolvedTodayCount)
        assertEquals(2, state.metrics.size)
        assertEquals("Tickets Assigned", state.metrics[0].label)
        assertEquals("2", state.metrics[0].valueLabel)
        assertEquals("Tickets Resolved", state.metrics[1].label)
        assertEquals("0", state.metrics[1].valueLabel)
        assertEquals(2, state.recentTickets.size)
        assertEquals("TCK-1002", state.recentTickets[0].ticketId)
        assertEquals("Printer setup", state.recentTickets[0].title)
        assertEquals("Bob Chen", state.recentTickets[0].employeeName)
        assertEquals("High", state.recentTickets[0].priorityLabel)
        assertEquals("RESOLVED", state.recentTickets[0].statusLabel)
        assertEquals("TCK-1001", state.recentTickets[1].ticketId)
        assertEquals("Laptop access request", state.recentTickets[1].title)
        assertEquals("Critical", state.recentTickets[1].priorityLabel)
    }

    @Test
    fun `view model reflects updated assigned tickets`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeAgentHomeRepository(
                initialState = AgentHomeWorkspaceSnapshot(
                    currentUser = sampleAgent(),
                    tickets = sampleTickets()
                )
            )
            val viewModel = AgentHomeViewModel(repository = repository, clock = now.toDateClock())

            advanceUntilIdle()

            val initialState = viewModel.uiState.value
            assertEquals(2, initialState.assignedTicketsCount)
            assertEquals(0, initialState.resolvedTodayCount)
            assertEquals("TCK-1002", initialState.recentTickets.first().ticketId)

            repository.emit(
                AgentHomeWorkspaceSnapshot(
                    currentUser = sampleAgent(fullName = "Aisha Khan"),
                    tickets = sampleTickets(
                        extra = Ticket(
                            ticketId = "TCK-1003",
                            employeeName = "Cara Diaz",
                            workspaceName = "SupportHub North America",
                            subject = "VPN issue",
                            priority = "Medium",
                            status = "OPEN",
                            assignedAgentId = "agent-1",
                            assignedAgentName = "Aisha Khan",
                            createdAt = 1_722_000_000_000,
                        )
                    )
                )
            )
            advanceUntilIdle()

            val updatedState = viewModel.uiState.value
            assertEquals("Aisha Khan", updatedState.header.agentName)
            assertEquals(3, updatedState.assignedTicketsCount)
            assertEquals(0, updatedState.resolvedTodayCount)
            assertEquals("TCK-1003", updatedState.recentTickets.first().ticketId)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `startup destination resolver routes active agent to agent dashboard`() {
        val destination = com.example.supporthub.features.authentication.utils.StartupDestinationResolver.resolve(sampleAgent())

        assertEquals(com.example.supporthub.features.authentication.utils.StartupDestination.AgentDashboard, destination)
    }

    private fun sampleAgent(fullName: String = "Alex Rivera"): User = User(
        uid = "agent-1",
        fullName = fullName,
        email = "alex.rivera@supporthub.io",
        phone = "+1 (512) 555-0118",
        location = "Austin, TX",
        jobTitle = "Senior Support Agent",
        department = "Customer Support",
        workspaceName = "SupportHub North America",
        requestedRole = "agent",
        approvedRole = "agent",
        status = "active",
        authProvider = "email",
        createdAt = 1_720_000_000_000,
        updatedAt = 1_720_000_000_000,
        lastLogin = 1_720_000_000_000
    )

    private fun sampleTickets(extra: Ticket? = null): List<Ticket> = buildList {
        add(
            Ticket(
                ticketId = "TCK-1001",
                employeeName = "Alice Wong",
                workspaceName = "SupportHub North America",
                subject = "Laptop access request",
                priority = "Critical",
                status = "OPEN",
                assignedAgentId = "agent-1",
                assignedAgentName = "Alex Rivera",
                createdAt = 1_720_000_000_000,
            )
        )
        add(
            Ticket(
                ticketId = "TCK-1002",
                employeeName = "Bob Chen",
                workspaceName = "SupportHub North America",
                subject = "Printer setup",
                priority = "High",
                status = "RESOLVED",
                assignedAgentId = "agent-1",
                assignedAgentName = "Alex Rivera",
                createdAt = 1_721_000_000_000,
                resolvedAt = 1_728_000_000_000,
            )
        )
        extra?.let { add(it) }
    }
}

private class FakeAgentHomeRepository(
    initialState: AgentHomeWorkspaceSnapshot,
) : AgentHomeRepository {

    private val state = MutableStateFlow(initialState)

    fun emit(snapshot: AgentHomeWorkspaceSnapshot) {
        state.value = snapshot
    }

    override fun observeWorkspaceSnapshot(): Flow<AgentHomeWorkspaceSnapshot> = state
}

private fun ZonedDateTime.toDate() = Date.from(toInstant())

private fun ZonedDateTime.toDateClock(): () -> Date = { toDate() }
