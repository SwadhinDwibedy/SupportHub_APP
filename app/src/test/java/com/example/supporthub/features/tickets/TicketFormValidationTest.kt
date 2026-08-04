package com.example.supporthub.features.tickets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketFormValidationTest {

    @Test
    fun `validate returns subject error when subject is blank`() {
        val result = validateTicketDraft(
            subject = "   ",
            description = "Cannot access payroll portal",
            selectedCategory = "Access",
            selectedPriority = "High"
        )

        assertTrue(result is TicketDraftValidationResult.Invalid)
        assertEquals(
            "Subject cannot be empty.",
            (result as TicketDraftValidationResult.Invalid).subjectError
        )
    }

    @Test
    fun `validate returns description error when description is blank`() {
        val result = validateTicketDraft(
            subject = "Payroll issue",
            description = "   ",
            selectedCategory = "Access",
            selectedPriority = "High"
        )

        assertTrue(result is TicketDraftValidationResult.Invalid)
        assertEquals(
            "Description cannot be empty.",
            (result as TicketDraftValidationResult.Invalid).descriptionError
        )
    }

    @Test
    fun `validate returns category error when category is missing`() {
        val result = validateTicketDraft(
            subject = "Payroll issue",
            description = "Cannot access payroll portal",
            selectedCategory = null,
            selectedPriority = "High"
        )

        assertTrue(result is TicketDraftValidationResult.Invalid)
        assertEquals(
            "Category must be selected.",
            (result as TicketDraftValidationResult.Invalid).categoryError
        )
    }

    @Test
    fun `validate returns priority error when priority is missing`() {
        val result = validateTicketDraft(
            subject = "Payroll issue",
            description = "Cannot access payroll portal",
            selectedCategory = "Access",
            selectedPriority = null
        )

        assertTrue(result is TicketDraftValidationResult.Invalid)
        assertEquals(
            "Priority must be selected.",
            (result as TicketDraftValidationResult.Invalid).priorityError
        )
    }

    @Test
    fun `validate trims values when draft is valid`() {
        val result = validateTicketDraft(
            subject = "  Payroll issue  ",
            description = "  Cannot access payroll portal  ",
            selectedCategory = "Access",
            selectedPriority = "High"
        )

        assertEquals(
            TicketDraftValidationResult.Valid(
                subject = "Payroll issue",
                description = "Cannot access payroll portal",
                category = "Access",
                priority = "High"
            ),
            result
        )
    }
}
