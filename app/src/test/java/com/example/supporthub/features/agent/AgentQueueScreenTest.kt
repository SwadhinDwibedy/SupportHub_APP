package com.example.supporthub.features.agent

import androidx.compose.ui.graphics.Color
import com.example.supporthub.features.tickets.TICKET_STATUS_OPEN
import com.example.supporthub.features.tickets.Ticket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AgentQueueScreenTest {

    private val zoneId = ZoneId.of("Asia/Kolkata")

    @Test
    fun `build agent queue ui state groups tickets into live colorful sections`() {
        val now = ZonedDateTime.of(2026, 7, 21, 18, 30, 0, 0, zoneId).toInstant()
        val state = buildAgentQueueUiState(
            currentUserId = "agent-1",
            currentAgentName = "Aisha Khan",
            workspaceName = "Acme",
            tickets = listOf(
                sampleTicket(
                    ticketId = "4019",
                    subject = "VPN access not working after macOS Sonoma upgrade",
                    status = TICKET_STATUS_OPEN,
                    priority = "High",
                    category = "IT Support",
                    createdAt = now.minusSeconds(240),
                    updatedAt = now.minusSeconds(60),
                ),
                sampleTicket(
                    ticketId = "4018",
                    subject = "Salesforce integration timeout errors on deal creation",
                    status = "PENDING",
                    priority = "Critical",
                    category = "CRM",
                    createdAt = now.minusSeconds(420),
                    updatedAt = now.minusSeconds(120),
                    assignedAgentName = "Aisha Khan",
                ),
                sampleTicket(
                    ticketId = "4017",
                    subject = "Request for new monitor stands and docking setup",
                    status = "RESOLVED",
                    priority = "Low",
                    category = "Hardware",
                    createdAt = now.minusSeconds(900),
                    updatedAt = now.minusSeconds(300),
                ),
                sampleTicket(
                    ticketId = "4999",
                    workspaceName = "Other",
                    subject = "Should be filtered out",
                    status = TICKET_STATUS_OPEN,
                    priority = "High",
                    category = "Other",
                    createdAt = now.minusSeconds(100),
                    updatedAt = now.minusSeconds(20),
                ),
            ),
            selectedFilter = AgentQueueFilter.ALL,
            now = now,
        )

        assertEquals("Aisha Khan", state.header.avatarInitials)
        assertEquals("Acme", state.header.workspaceName)
        assertEquals(3, state.totalTicketsCount)
        assertEquals(1, state.openCount)
        assertEquals(1, state.inProgressCount)
        assertEquals(1, state.resolvedCount)
        assertEquals(2, state.summaryCards.size)
        assertEquals(listOf("High priority", "In progress"), state.sectionCards.map { it.title })
        assertEquals(listOf("#4019", "#4018", "#4017"), state.sectionCards.flatMap { it.tickets }.map { it.displayTicketId })
        assertTrue(state.realtimeBanner.contains("live"))
        assertEquals(Color(0xFF2563EB).value.toLong(), state.sectionCards[0].accentColor.value.toLong())
    }

    @Test
    fun `empty queue ui state still presents premium placeholders`() {
        val state = buildAgentQueueUiState(
            currentAgentName = "Aisha Khan",
            workspaceName = "Acme",
            tickets = emptyList(),
            now = ZonedDateTime.of(2026, 7, 21, 18, 30, 0, 0, zoneId).toInstant(),
        )

        assertEquals(0, state.totalTicketsCount)
        assertEquals(0, state.openCount)
        assertEquals(0, state.inProgressCount)
        assertEquals(0, state.resolvedCount)
        assertTrue(state.sectionCards.all { it.tickets.isEmpty() })
    }

    private fun sampleTicket(
        ticketId: String,
        subject: String,
        status: String,
        priority: String,
        category: String,
        createdAt: Any,
        updatedAt: Any,
        workspaceName: String = "Acme",
        assignedAgentName: String? = null,
    ): Ticket = Ticket(
        ticketId = ticketId,
        employeeUid = "employee-$ticketId",
        employeeName = "Requester $ticketId",
        employeeEmail = "requester$ticketId@example.com",
        workspaceName = workspaceName,
        subject = subject,
        description = "Description $ticketId",
        category = category,
        priority = priority,
        status = status,
        assignedAgentName = assignedAgentName,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}



