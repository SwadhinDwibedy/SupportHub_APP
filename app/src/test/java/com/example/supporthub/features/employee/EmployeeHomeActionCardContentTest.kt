package com.example.supporthub.features.employee

import com.example.supporthub.features.tickets.TicketUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class EmployeeHomeActionCardContentTest {

    @Test
    fun `build action card state uses create ticket hero copy when there are no tickets`() {
        val state = buildEmployeeHomeActionCardState(
            userName = "Sarah",
            uiState = TicketUiState(tickets = emptyList())
        )

        assertEquals("Create a Ticket", state.title)
        assertEquals("Need IT support or have a request? We are here to help.", state.description)
        assertEquals("New Ticket", state.primaryActionLabel)
    }

    @Test
    fun `build action card state keeps create ticket hero copy when tickets exist`() {
        val state = buildEmployeeHomeActionCardState(
            userName = "Sarah",
            uiState = TicketUiState(
                tickets = listOf(
                    com.example.supporthub.features.tickets.Ticket(
                        subject = "Laptop issue",
                        status = "OPEN",
                        priority = "High"
                    )
                )
            )
        )

        assertEquals("Create a Ticket", state.title)
        assertEquals("Need IT support or have a request? We are here to help.", state.description)
        assertEquals("New Ticket", state.primaryActionLabel)
    }
}
