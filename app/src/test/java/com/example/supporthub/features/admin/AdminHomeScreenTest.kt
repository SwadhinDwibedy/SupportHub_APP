package com.example.supporthub.features.admin

import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.tickets.TICKET_STATUS_OPEN
import com.example.supporthub.features.tickets.Ticket
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminHomeScreenTest {

    private val zoneId: ZoneId = ZoneId.of("Asia/Kolkata")

    @Test
    fun `organization health aggregates active users departments and uptime from live data`() {
        val users = listOf(
            sampleUser(uid = "1", department = "Support", status = "active"),
            sampleUser(uid = "2", department = "Support", status = "active"),
            sampleUser(uid = "3", department = "HR", status = "active"),
            sampleUser(uid = "4", department = "", status = "pending")
        )
        val tickets = listOf(
            sampleTicket(ticketId = "1", status = "RESOLVED"),
            sampleTicket(ticketId = "2", status = "IN_PROGRESS"),
            sampleTicket(ticketId = "3", status = TICKET_STATUS_OPEN),
            sampleTicket(ticketId = "4", status = "RESOLVED")
        )

        val state = buildOrganizationHealthState(users = users, tickets = tickets)

        assertEquals("75", state.scoreLabel)
        assertEquals("Excellent • Uptime 50.00%", state.summaryLabel)
        assertEquals("3", state.totalUsersLabel)
        assertEquals("2", state.departmentsLabel)
    }

    @Test
    fun `todays tickets aggregates created resolved and pending for current local day`() {
        val now = ZonedDateTime.of(2026, 7, 21, 15, 30, 0, 0, zoneId)
        val tickets = listOf(
            sampleTicket(
                ticketId = "1",
                status = "RESOLVED",
                createdAt = now.minusHours(3).toInstant(),
                resolvedAt = now.minusHours(1).toInstant()
            ),
            sampleTicket(
                ticketId = "2",
                status = TICKET_STATUS_OPEN,
                createdAt = now.minusHours(2).toInstant()
            ),
            sampleTicket(
                ticketId = "3",
                status = "IN_PROGRESS",
                createdAt = now.minusHours(5).toInstant()
            ),
            sampleTicket(
                ticketId = "4",
                status = "RESOLVED",
                createdAt = now.minusDays(1).toInstant(),
                resolvedAt = now.minusDays(1).plusHours(2).toInstant()
            )
        )

        val state = buildTodayTicketsState(tickets = tickets, now = now)

        assertEquals("3", state.createdLabel)
        assertEquals("1", state.resolvedLabel)
        assertEquals("2", state.pendingLabel)
    }

    private fun sampleUser(
        uid: String,
        department: String,
        status: String,
    ): User = User(
        uid = uid,
        fullName = "User $uid",
        email = "user$uid@example.com",
        department = department,
        status = status,
        workspaceName = "Acme"
    )

    private fun sampleTicket(
        ticketId: String,
        status: String,
        createdAt: Any? = ZonedDateTime.of(2026, 7, 21, 9, 0, 0, 0, zoneId).toInstant(),
        resolvedAt: Any? = null,
    ): Ticket = Ticket(
        ticketId = ticketId,
        employeeUid = "employee-$ticketId",
        employeeName = "Employee $ticketId",
        employeeEmail = "employee$ticketId@example.com",
        workspaceName = "Acme",
        subject = "Ticket $ticketId",
        description = "Description",
        category = "IT",
        priority = "High",
        status = status,
        createdAt = createdAt,
        resolvedAt = resolvedAt,
    )
}
