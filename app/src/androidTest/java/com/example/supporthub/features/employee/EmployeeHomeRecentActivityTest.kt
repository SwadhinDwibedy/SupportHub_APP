package com.example.supporthub.features.employee

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmployeeHomeRecentActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recentActivity_showsCompactListSemanticsForTickets() {
        composeTestRule.setContent {
            EmployeeTicketSection(
                uiState = TicketUiState(
                    tickets = listOf(
                        Ticket(
                            ticketId = "4021",
                            subject = "Software Access",
                            description = "VPN access approved and completed.",
                            category = "Software",
                            priority = "Low",
                            status = "RESOLVED"
                        ),
                        Ticket(
                            ticketId = "4019",
                            subject = "Password assistance",
                            description = "Agent has replied with additional steps.",
                            category = "Access",
                            priority = "Medium",
                            status = "IN_PROGRESS",
                            assignedAgentName = "Alex Rivera"
                        )
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag("employee_recent_activity_section").assertIsDisplayed()
        composeTestRule.onNodeWithTag("employee_recent_activity_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("employee_recent_activity_see_all").assertIsDisplayed()
        composeTestRule.onNodeWithTag("employee_recent_activity_item_4021").assertIsDisplayed()
        composeTestRule.onNodeWithTag("employee_recent_activity_item_4019").assertIsDisplayed()
        composeTestRule.onNodeWithTag("employee_recent_activity_divider_4019").assertIsDisplayed()
    }
}
