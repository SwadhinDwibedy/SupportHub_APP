package com.example.supporthub.features.tickets

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketBottomSheetStateTest {

    @Test
    fun `submit is disabled when form is incomplete`() {
        val state = TicketUiState(
            subject = "Printer issue",
            description = "Printer not responding"
        )

        assertFalse(state.isSubmitEnabled)
    }

    @Test
    fun `submit is enabled when all required fields are filled and not submitting`() {
        val state = TicketUiState(
            subject = "Printer issue",
            description = "Printer not responding",
            selectedCategory = "Hardware",
            selectedPriority = "High"
        )

        assertTrue(state.isSubmitEnabled)
    }

    @Test
    fun `submit is disabled while submitting even when form is valid`() {
        val state = TicketUiState(
            subject = "Printer issue",
            description = "Printer not responding",
            selectedCategory = "Hardware",
            selectedPriority = "High",
            isSubmitting = true
        )

        assertFalse(state.isSubmitEnabled)
    }
}
