package com.example.supporthub.features.admin

import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.tickets.TICKET_STATUS_OPEN
import com.example.supporthub.features.tickets.Ticket
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminHomeFirebaseSummaryTest {

    private val zoneId = ZoneId.of("Asia/Kolkata")

    @Test
    fun `organization health counts active users from same workspace only`() {
        val users = listOf(
            sampleUser(uid = "1", workspaceName = "Acme", department = "Support", status = "active"),
            sampleUser(uid = "2", workspaceName = "Acme", department = "HR", status = "active"),
            sampleUser(uid = "3", workspaceName = "Acme", department = "", status = "pending"),
            sampleUser(uid = "4", workspaceName = "Other", department = "Finance", status = "active")
        )
        val tickets = listOf(
            sampleTicket(ticketId = "1", workspaceName = "Acme", status = "RESOLVED"),
            sampleTicket(ticketId = "2", workspaceName = "Acme", status = TICKET_STATUS_OPEN),
            sampleTicket(ticketId = "3", workspaceName = "Other", status = "RESOLVED")
        )

        val state = buildOrganizationHealthState(
            users = users.filter { it.workspaceName == "Acme" },
            tickets = tickets.filter { it.workspaceName == "Acme" }
        )

        assertEquals("50", state.scoreLabel)
        assertEquals("Stable • Uptime 50.00%", state.summaryLabel)
        assertEquals("2", state.totalUsersLabel)
        assertEquals("2", state.departmentsLabel)
    }

    @Test
    fun `today tickets counts only current workspace live ticket data`() {
        val now = ZonedDateTime.of(2026, 7, 21, 18, 0, 0, 0, zoneId)
        val tickets = listOf(
            sampleTicket(
                ticketId = "1",
                workspaceName = "Acme",
                status = "RESOLVED",
                createdAt = now.minusHours(3).toInstant(),
                resolvedAt = now.minusHours(1).toInstant()
            ),
            sampleTicket(
                ticketId = "2",
                workspaceName = "Acme",
                status = TICKET_STATUS_OPEN,
                createdAt = now.minusHours(2).toInstant()
            ),
            sampleTicket(
                ticketId = "3",
                workspaceName = "Other",
                status = TICKET_STATUS_OPEN,
                createdAt = now.minusHours(1).toInstant()
            )
        )

        val state = buildTodayTicketsState(
            tickets = tickets.filter { it.workspaceName == "Acme" },
            now = now
        )

        assertEquals("2", state.createdLabel)
        assertEquals("1", state.resolvedLabel)
        assertEquals("1", state.pendingLabel)
    }

    @Test
    fun `recent users keeps newest five workspace users with profile names and dates`() {
        val now = ZonedDateTime.of(2026, 7, 21, 18, 0, 0, 0, zoneId)
        val users = listOf(
            sampleUser(uid = "1", fullName = "Alpha", createdAt = now.minusDays(5).toInstant().toEpochMilli()),
            sampleUser(uid = "2", fullName = "Bravo", createdAt = now.minusDays(3).toInstant().toEpochMilli()),
            sampleUser(uid = "3", fullName = "Charlie", createdAt = now.minusDays(4).toInstant().toEpochMilli()),
            sampleUser(uid = "4", fullName = "Delta", createdAt = now.minusDays(1).toInstant().toEpochMilli()),
            sampleUser(uid = "5", fullName = "Echo", createdAt = now.minusDays(2).toInstant().toEpochMilli()),
            sampleUser(uid = "6", fullName = "Foxtrot", createdAt = now.toInstant().toEpochMilli()),
        )

        val state = buildRecentUsersState(users = users, now = now)
        val expectedDates = listOf(
            now,
            now.minusDays(1),
            now.minusDays(2),
            now.minusDays(3),
            now.minusDays(4),
        ).map { dateTime ->
            dateTime.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", java.util.Locale.ENGLISH)).uppercase(java.util.Locale.ENGLISH)
        }

        assertEquals(5, state.items.size)
        assertEquals(listOf("Foxtrot", "Delta", "Echo", "Bravo", "Charlie"), state.items.map { it.profileName })
        assertEquals(expectedDates, state.items.map { it.dateLabel })
    }

    @Test
    fun `recent tickets keeps newest five workspace tickets with names doc ids created dates and statuses`() {
        val now = ZonedDateTime.of(2026, 7, 21, 18, 0, 0, 0, zoneId)
        val tickets = listOf(
            sampleTicket(ticketId = "1", subject = "One", status = "OPEN", createdAt = now.minusDays(5).toInstant()),
            sampleTicket(ticketId = "2", subject = "Two", status = "IN_PROGRESS", createdAt = now.minusDays(3).toInstant()),
            sampleTicket(ticketId = "3", subject = "Three", status = "RESOLVED", createdAt = now.minusDays(4).toInstant()),
            sampleTicket(ticketId = "4", subject = "Four", status = "OPEN", createdAt = now.minusDays(1).toInstant()),
            sampleTicket(ticketId = "5", subject = "Five", status = "OPEN", createdAt = now.minusDays(2).toInstant()),
            sampleTicket(ticketId = "6", subject = "Six", status = "OPEN", createdAt = now.toInstant()),
        )

        val state = buildRecentTicketsState(tickets = tickets, now = now)
        val expectedDates = listOf(
            now,
            now.minusDays(1),
            now.minusDays(2),
            now.minusDays(3),
            now.minusDays(4),
        ).map { dateTime ->
            dateTime.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", java.util.Locale.ENGLISH)).uppercase(java.util.Locale.ENGLISH)
        }

        assertEquals(5, state.items.size)
        assertEquals(listOf("Six", "Four", "Five", "Two", "Three"), state.items.map { it.name })
        assertEquals(listOf("6", "4", "5", "2", "3"), state.items.map { it.documentId })
        assertEquals(expectedDates, state.items.map { it.createdDateLabel })
        assertEquals(listOf("OPEN", "OPEN", "OPEN", "IN_PROGRESS", "RESOLVED"), state.items.map { it.statusLabel })
    }


    private fun sampleUser(
        uid: String,
        workspaceName: String = "Acme",
        department: String = "Support",
        status: String = "active",
        fullName: String = "User $uid",
        createdAt: Long = 1L,
    ): User = User(
        uid = uid,
        fullName = fullName,
        email = "user$uid@example.com",
        department = department,
        status = status,
        workspaceName = workspaceName,
        createdAt = createdAt,
    )

    private fun sampleTicket(
        ticketId: String,
        workspaceName: String = "Acme",
        status: String,
        createdAt: Any? = ZonedDateTime.of(2026, 7, 21, 10, 0, 0, 0, zoneId).toInstant(),
        resolvedAt: Any? = null,
        subject: String = "Ticket $ticketId",
    ): Ticket = Ticket(
        ticketId = ticketId,
        employeeUid = "employee-$ticketId",
        employeeName = "Employee $ticketId",
        employeeEmail = "employee$ticketId@example.com",
        workspaceName = workspaceName,
        subject = subject,
        description = "Description",
        category = "IT",
        priority = "High",
        status = status,
        createdAt = createdAt,
        resolvedAt = resolvedAt,
    )
}
