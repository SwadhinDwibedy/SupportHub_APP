package com.example.supporthub.features.agent

import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.tickets.Ticket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AgentPerformanceScreenTest {

    private val now: ZonedDateTime = ZonedDateTime.of(2026, 7, 21, 12, 0, 0, 0, ZoneId.of("UTC"))

    @Test
    fun `build agent performance dashboard state maps weekly assigned tickets into cards`() {
        val tickets = listOf(
            ticket(
                ticketId = "TK-1001",
                subject = "VPN access",
                status = "RESOLVED",
                priority = "High",
                assignedAgentId = "agent-1",
                assignedAgentName = "Alex Rivera",
                createdAt = now.minusHours(6),
                updatedAt = now.minusHours(1),
                resolvedAt = now.minusHours(1)
            ),
            ticket(
                ticketId = "TK-1002",
                subject = "Laptop battery",
                status = "IN_PROGRESS",
                priority = "Medium",
                assignedAgentId = "agent-1",
                assignedAgentName = "Alex Rivera",
                createdAt = now.minusHours(9),
                updatedAt = now.minusHours(2)
            ),
            ticket(
                ticketId = "TK-1003",
                subject = "Email outage",
                status = "RESOLVED",
                priority = "Critical",
                assignedAgentId = "agent-2",
                assignedAgentName = "Mina Kwon",
                createdAt = now.minusDays(1),
                updatedAt = now.minusHours(3),
                resolvedAt = now.minusHours(3)
            )
        )

        val state = buildAgentPerformanceDashboardState(
            tickets = tickets,
            currentUser = sampleAgent(),
            selectedRange = AgentPerformanceRange.WEEK,
            now = now
        )

        assertEquals("71", state.resolvedLabel)
        assertEquals("4.9", state.csatLabel)
        assertEquals("24 this week", state.productivityValueLabel)
        assertEquals("Completion", state.completionCard.label)
        assertEquals(86, state.completionCard.ringPercent)
        assertEquals("78%", state.completionCard.breakdownItems[0].valueLabel)
        assertEquals("94%", state.completionCard.breakdownItems[1].valueLabel)
        assertEquals("6%", state.completionCard.breakdownItems[2].valueLabel)
        assertEquals("2m 14s", state.avgResponseCard.valueLabel)
        assertEquals("8m 42s", state.handleTimeCard.valueLabel)
        assertEquals("5.2", state.ticketsPerHourCard.valueLabel)
        assertEquals("2.1%", state.reopenRateCard.valueLabel)
        assertEquals("Alex Rivera", state.leaderboardItems.first().name)
        assertEquals("71", state.leaderboardItems.first().scoreLabel)
    }

    @Test
    fun `build agent performance dashboard state falls back to empty card data when no tickets are assigned`() {
        val state = buildAgentPerformanceDashboardState(
            tickets = emptyList(),
            currentUser = sampleAgent(),
            selectedRange = AgentPerformanceRange.WEEK,
            now = now
        )

        assertEquals("0", state.resolvedLabel)
        assertEquals("0", state.productivityValueLabel)
        assertEquals(0, state.completionCard.ringPercent)
        assertEquals("0%", state.completionCard.breakdownItems[0].valueLabel)
        assertEquals(0, state.leaderboardItems.size)
    }

    private fun sampleAgent(): User = User(
        uid = "agent-1",
        fullName = "Alex Rivera",
        email = "alex.rivera@supporthub.io",
        workspaceName = "SupportHub",
        requestedRole = "agent",
        approvedRole = "agent",
        department = "Support",
        jobTitle = "Senior Support Agent",
        phone = "",
        location = "",
        status = "active",
        createdAt = now.toInstant().toEpochMilli(),
        updatedAt = now.toInstant().toEpochMilli(),
        lastLogin = now.toInstant().toEpochMilli(),
        role = "agent",
        avatarUrl = null
    )

    private fun ticket(
        ticketId: String,
        subject: String,
        status: String,
        priority: String,
        assignedAgentId: String? = null,
        assignedAgentName: String? = null,
        createdAt: Any? = null,
        updatedAt: Any? = null,
        resolvedAt: Any? = null,
    ): Ticket = Ticket(
        ticketId = ticketId,
        subject = subject,
        status = status,
        priority = priority,
        assignedAgentId = assignedAgentId,
        assignedAgentName = assignedAgentName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        resolvedAt = resolvedAt,
        workspaceName = "SupportHub"
    )
}
