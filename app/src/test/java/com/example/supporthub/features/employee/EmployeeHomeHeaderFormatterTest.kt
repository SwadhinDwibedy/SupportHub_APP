package com.example.supporthub.features.employee

import androidx.compose.ui.graphics.Color
import com.example.supporthub.features.tickets.Ticket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class EmployeeHomeHeaderFormatterTest {

    private val zoneId = ZoneId.of("Asia/Calcutta")

    @Test
    fun `summary counts group tickets by open in progress and resolved statuses`() {
        val summary = buildEmployeeTicketSummaryState(
            listOf(
                sampleTicket(ticketId = "4025", status = "OPEN"),
                sampleTicket(ticketId = "4019", status = "IN_PROGRESS"),
                sampleTicket(ticketId = "4018", status = "PENDING"),
                sampleTicket(ticketId = "4012", status = "RESOLVED"),
                sampleTicket(ticketId = "4011", status = "CLOSED"),
            )
        )

        assertEquals(1, summary.openCount)
        assertEquals(2, summary.inProgressCount)
        assertEquals(2, summary.resolvedCount)
    }

    @Test
    fun `build returns morning greeting and uppercase date with first name`() {
        val dateTime = ZonedDateTime.of(2026, 8, 15, 9, 30, 0, 0, zoneId)

        val state = buildEmployeeHomeHeaderState(
            fullName = "Sarah Johnson",
            dateTime = dateTime
        )

        assertEquals("SATURDAY, 15 AUGUST", state.dateLabel)
        assertEquals("Good morning, Sarah", state.greeting)
    }

    @Test
    fun `build returns afternoon greeting`() {
        val dateTime = ZonedDateTime.of(2026, 8, 15, 14, 0, 0, 0, zoneId)

        val state = buildEmployeeHomeHeaderState(
            fullName = "Sarah Johnson",
            dateTime = dateTime
        )

        assertEquals("Good afternoon, Sarah", state.greeting)
    }

    @Test
    fun `build returns evening greeting`() {
        val dateTime = ZonedDateTime.of(2026, 8, 15, 19, 0, 0, 0, zoneId)

        val state = buildEmployeeHomeHeaderState(
            fullName = "Sarah Johnson",
            dateTime = dateTime
        )

        assertEquals("Good evening, Sarah", state.greeting)
    }

    @Test
    fun `build returns night greeting in late hours`() {
        val dateTime = ZonedDateTime.of(2026, 8, 15, 23, 15, 0, 0, zoneId)

        val state = buildEmployeeHomeHeaderState(
            fullName = "Sarah Johnson",
            dateTime = dateTime
        )

        assertEquals("Good night, Sarah", state.greeting)
    }

    @Test
    fun `build returns night greeting before morning starts`() {
        val dateTime = ZonedDateTime.of(2026, 8, 15, 4, 45, 0, 0, zoneId)

        val state = buildEmployeeHomeHeaderState(
            fullName = "Sarah Johnson",
            dateTime = dateTime
        )

        assertEquals("Good night, Sarah", state.greeting)
    }

    @Test
    fun `build falls back to email prefix when name is blank`() {
        val dateTime = ZonedDateTime.of(2026, 8, 15, 9, 30, 0, 0, zoneId)

        val state = buildEmployeeHomeHeaderState(
            fullName = "   ",
            email = "employee.user@supporthub.com",
            dateTime = dateTime
        )

        assertEquals("Good morning, employee", state.greeting)
    }

    @Test
    fun `recent activity items keep only five latest tickets`() {
        val activities = buildEmployeeRecentActivityItems(
            (1..6).map { index ->
                sampleTicket(
                    ticketId = "ABCD$index",
                    status = if (index % 2 == 0) "OPEN" else "RESOLVED"
                )
            }
        )

        assertEquals(5, activities.size)
        assertEquals("ABCD1", activities.first().ticketId)
        assertEquals("ABCD5", activities.last().ticketId)
    }

    @Test
    fun `recent activity item formats first four digits and open status`() {
        val activity = buildEmployeeRecentActivityItems(
            listOf(sampleTicket(ticketId = "987654321", status = "OPEN", category = "Hardware"))
        ).single()

        assertEquals("9876", activity.displayId)
        assertEquals("Open", activity.statusLabel)
        assertTrue(activity.title.contains("9876"))
        assertEquals(EmployeeRecentActivityIcon.TICKET_OPEN, activity.icon)
    }

    @Test
    fun `recent activity item maps in progress and resolved variants`() {
        val activities = buildEmployeeRecentActivityItems(
            listOf(
                sampleTicket(ticketId = "43219876", status = "PENDING", assignedAgentName = "Alex"),
                sampleTicket(ticketId = "55556666", status = "CLOSED", category = "Network")
            )
        )

        assertEquals("In Progress", activities[0].statusLabel)
        assertEquals(EmployeeRecentActivityIcon.TICKET_IN_PROGRESS, activities[0].icon)
        assertEquals("Resolved", activities[1].statusLabel)
        assertEquals(EmployeeRecentActivityIcon.TICKET_RESOLVED, activities[1].icon)
    }

    @Test
    fun `summary card styles expose embedded accent strip treatment`() {
        val openStyle = employeeSummaryCardStyle(EmployeeSummaryCardKind.OPEN)
        val inProgressStyle = employeeSummaryCardStyle(EmployeeSummaryCardKind.IN_PROGRESS)
        val resolvedStyle = employeeSummaryCardStyle(EmployeeSummaryCardKind.RESOLVED)

        assertEquals("Open Tickets", openStyle.label)
        assertEquals("In Progress", inProgressStyle.label)
        assertEquals("Resolved", resolvedStyle.label)
        assertEquals("orange", openStyle.sidecapName)
        assertEquals("blue", inProgressStyle.sidecapName)
        assertEquals("green", resolvedStyle.sidecapName)
        assertTrue(openStyle.whiteLeftPanelOnly)
        assertTrue(inProgressStyle.whiteLeftPanelOnly)
        assertTrue(resolvedStyle.whiteLeftPanelOnly)
        assertTrue(openStyle.usesSoftBackground)
        assertTrue(inProgressStyle.usesSoftBackground)
        assertTrue(resolvedStyle.usesSoftBackground)
        assertTrue(openStyle.usesReferenceCardShadow)
        assertTrue(inProgressStyle.usesReferenceCardShadow)
        assertTrue(resolvedStyle.usesReferenceCardShadow)
        assertTrue(openStyle.usesThinLeftAccent)
        assertTrue(inProgressStyle.usesThinLeftAccent)
        assertTrue(resolvedStyle.usesThinLeftAccent)
        assertFalse(openStyle.usesCapAccent)
        assertFalse(inProgressStyle.usesCapAccent)
        assertFalse(resolvedStyle.usesCapAccent)
        assertEquals(24, openStyle.cornerRadiusDp)
        assertEquals(24, inProgressStyle.cornerRadiusDp)
        assertEquals(24, resolvedStyle.cornerRadiusDp)
        assertEquals(5, openStyle.leftAccentWidthDp)
        assertEquals(5, inProgressStyle.leftAccentWidthDp)
        assertEquals(5, resolvedStyle.leftAccentWidthDp)
        assertEquals(0, openStyle.capHeightDp)
        assertEquals(0, inProgressStyle.capHeightDp)
        assertEquals(0, resolvedStyle.capHeightDp)
        assertEquals(0, openStyle.contentEmphasisLevel)
        assertEquals(0, inProgressStyle.contentEmphasisLevel)
        assertEquals(0, resolvedStyle.contentEmphasisLevel)
        assertEquals(Color(0xFFF59E0B).value.toLong(), openStyle.sidecapColor.value.toLong())
        assertEquals(Color(0xFF3B82F6).value.toLong(), inProgressStyle.sidecapColor.value.toLong())
        assertEquals(Color(0xFF22C55E).value.toLong(), resolvedStyle.sidecapColor.value.toLong())
    }

    @Test
    fun `summary cards use reduced non compact width fraction`() {
        assertEquals(0.29f, employeeSummaryCardWidthFraction(isCompactLayout = false), 0.0001f)
        assertEquals(1f, employeeSummaryCardWidthFraction(isCompactLayout = true), 0.0001f)
    }

    @Test
    fun `summary cards use reduced spacing`() {
        assertEquals(6, employeeSummaryCardSpacingDp(isCompactLayout = false))
        assertEquals(6, employeeSummaryCardSpacingDp(isCompactLayout = true))
    }

    @Test
    fun `summary cards use reduced bottom padding`() {
        assertEquals(18, employeeSummaryCardHorizontalPaddingDp())
        assertEquals(18, employeeSummaryCardTopPaddingDp())
        assertEquals(12, employeeSummaryCardBottomPaddingDp())
    }

    private fun sampleTicket(
        ticketId: String,
        status: String,
        category: String = "IT",
        assignedAgentName: String? = null,
    ): Ticket = Ticket(
        ticketId = ticketId,
        employeeUid = "employee-1",
        employeeName = "Sarah Johnson",
        employeeEmail = "sarah@example.com",
        workspaceName = "Acme",
        subject = "Subject $ticketId",
        description = "Description $ticketId",
        category = category,
        priority = "High",
        status = status,
        assignedAgentName = assignedAgentName,
    )
}
