package com.example.supporthub.features.admin

import com.example.supporthub.features.tickets.Ticket
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class AdminDashboardMetricsFormatterTest {

    private val zoneId = ZoneId.of("Asia/Calcutta")
    private val now = ZonedDateTime.of(2026, 8, 15, 9, 30, 0, 0, zoneId)

    @Test
    fun `build dashboard metrics counts this month total open tickets and last month delta`() {
        val state = buildAdminDashboardMetricsState(
            tickets = listOf(
                sampleTicket(ticketId = "1", createdAt = zdt(2026, 8, 1), status = "OPEN"),
                sampleTicket(ticketId = "2", createdAt = zdt(2026, 8, 5), status = "IN_PROGRESS"),
                sampleTicket(ticketId = "3", createdAt = zdt(2026, 8, 10), status = "RESOLVED"),
                sampleTicket(ticketId = "4", createdAt = zdt(2026, 7, 2), status = "OPEN"),
                sampleTicket(ticketId = "5", createdAt = zdt(2026, 7, 20), status = "RESOLVED"),
                sampleTicket(ticketId = "6", createdAt = zdt(2026, 6, 20), status = "OPEN"),
            ),
            now = now,
        )

        assertEquals("TOTAL TICKETS THIS MONTH", state.label)
        assertEquals("3", state.totalTicketsLabel)
        assertEquals("+50% vs last month", state.deltaLabel)
        assertEquals("2 open", state.openTicketsLabel)
    }

    @Test
    fun `build dashboard metrics uses zero delta fallback when previous month has no tickets`() {
        val state = buildAdminDashboardMetricsState(
            tickets = listOf(
                sampleTicket(ticketId = "1", createdAt = zdt(2026, 8, 1), status = "OPEN"),
                sampleTicket(ticketId = "2", createdAt = zdt(2026, 8, 5), status = "RESOLVED"),
            ),
            now = now,
        )

        assertEquals("2", state.totalTicketsLabel)
        assertEquals("0% vs last month", state.deltaLabel)
        assertEquals("1 open", state.openTicketsLabel)
    }

    @Test
    fun `build dashboard metrics keeps negative delta when current month drops`() {
        val state = buildAdminDashboardMetricsState(
            tickets = listOf(
                sampleTicket(ticketId = "1", createdAt = zdt(2026, 8, 1), status = "OPEN"),
                sampleTicket(ticketId = "2", createdAt = zdt(2026, 7, 1), status = "OPEN"),
                sampleTicket(ticketId = "3", createdAt = zdt(2026, 7, 2), status = "OPEN"),
                sampleTicket(ticketId = "4", createdAt = zdt(2026, 7, 3), status = "RESOLVED"),
                sampleTicket(ticketId = "5", createdAt = zdt(2026, 7, 4), status = "RESOLVED"),
            ),
            now = now,
        )

        assertEquals("1", state.totalTicketsLabel)
        assertEquals("-75% vs last month", state.deltaLabel)
        assertEquals("1 open", state.openTicketsLabel)
    }

    @Test
    fun `build dashboard metrics counts firebase timestamp tickets from firestore`() {
        val state = buildAdminDashboardMetricsState(
            tickets = listOf(
                sampleTicket(ticketId = "1", createdAt = firebaseTimestamp(2026, 8, 1), status = "OPEN"),
                sampleTicket(ticketId = "2", createdAt = firebaseTimestamp(2026, 8, 5), status = "OPEN"),
                sampleTicket(ticketId = "3", createdAt = firebaseTimestamp(2026, 8, 10), status = "OPEN"),
                sampleTicket(ticketId = "4", createdAt = firebaseTimestamp(2026, 8, 12), status = "OPEN"),
            ),
            now = now,
        )

        assertEquals("4", state.totalTicketsLabel)
        assertEquals("4 open", state.openTicketsLabel)
    }

    private fun sampleTicket(
        ticketId: String,
        createdAt: Any,
        status: String,
    ): Ticket = Ticket(
        ticketId = ticketId,
        employeeUid = "employee-1",
        employeeName = "Alex",
        employeeEmail = "alex@supporthub.com",
        workspaceName = "Acme",
        subject = "Subject $ticketId",
        description = "Description $ticketId",
        category = "IT",
        priority = "High",
        status = status,
        createdAt = createdAt,
    )

    private fun zdt(year: Int, month: Int, day: Int): Instant {
        return ZonedDateTime.of(year, month, day, 9, 0, 0, 0, zoneId).toInstant()
    }

    private fun firebaseTimestamp(year: Int, month: Int, day: Int): Timestamp {
        return Timestamp(zdt(year, month, day))
    }
}
